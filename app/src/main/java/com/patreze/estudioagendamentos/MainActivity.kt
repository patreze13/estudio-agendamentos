package com.patreze.estudioagendamentos

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class Cliente(
    val id: Long,
    val nome: String,
    val contato: String
)

data class Agendamento(
    val id: Long,
    val clienteId: Long,
    val nomeCliente: String,
    val contato: String,
    val data: String,
    val tipo: String,
    val observacoes: String,
    val valorTotal: Double,
    val sinal: Double,
    val status: String
)

data class Entrega(
    val id: Long,
    val agendamentoId: Long,
    val nomeCliente: String,
    val dataEnsaio: String,
    val dataEntrega: String,
    val status: String
)

class BancoHelper(context: android.content.Context) :
    SQLiteOpenHelper(context, "estudio_agendamentos.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE clientes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                contato TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE agendamentos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente_id INTEGER NOT NULL,
                data TEXT NOT NULL,
                tipo TEXT NOT NULL,
                observacoes TEXT,
                valor_total REAL NOT NULL,
                sinal REAL NOT NULL,
                status TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE entregas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                agendamento_id INTEGER NOT NULL,
                data_entrega TEXT NOT NULL,
                status TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS entregas")
        db.execSQL("DROP TABLE IF EXISTS agendamentos")
        db.execSQL("DROP TABLE IF EXISTS clientes")
        onCreate(db)
    }
}

class MainActivity : Activity() {

    private lateinit var banco: BancoHelper

    private val formatoData =
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        banco = BancoHelper(this)

        mostrarMenu()
    }

    private fun baseLayout(): LinearLayout {

        val layout = LinearLayout(this)

        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)

        return layout
    }

    private fun titulo(texto: String): TextView {

        return TextView(this).apply {
            text = texto
            textSize = 24f
            setPadding(0, 0, 0, 30)
        }
    }

    private fun botao(
        texto: String,
        acao: () -> Unit
    ): Button {

        return Button(this).apply {
            text = texto
            setOnClickListener {
                acao()
            }
        }
    }

    private fun mostrarMenu() {

        val layout = baseLayout()

        layout.gravity = Gravity.CENTER_HORIZONTAL

        layout.addView(
            titulo("ESTÚDIO")
        )

        layout.addView(
            botao("AGENDAMENTOS") {
                listarAgendamentos()
            }
        )

        layout.addView(
            botao("ENTREGAS") {
                listarEntregas()
            }
        )

        layout.addView(
            botao("NOVO AGENDAMENTO") {
                novoAgendamento()
            }
        )

        setContentView(layout)
    }

    private fun novoAgendamento(
        agendamentoEditar: Agendamento? = null
    ) {

        val layout = baseLayout()

        layout.addView(
            titulo(
                if (agendamentoEditar == null)
                    "NOVO AGENDAMENTO"
                else
                    "EDITAR AGENDAMENTO"
            )
        )

        val nome = EditText(this)
        nome.hint = "Nome do cliente"

        val contato = EditText(this)
        contato.hint = "Contato"

        val data = EditText(this)
        data.hint = "Data do ensaio"
        data.isFocusable = false

        val tipo = EditText(this)
        tipo.hint = "Tipo de ensaio"

        val observacoes = EditText(this)
        observacoes.hint = "Observações"
        observacoes.minLines = 4
        observacoes.gravity = Gravity.TOP

        val valor = EditText(this)
        valor.hint = "Valor total"
        valor.inputType = 8194

        val sinal = EditText(this)
        sinal.hint = "Valor do sinal (0 se não houver)"
        sinal.inputType = 8194

        layout.addView(nome)
        layout.addView(contato)
        layout.addView(data)
        layout.addView(tipo)
        layout.addView(observacoes)
        layout.addView(valor)
        layout.addView(sinal)

        data.setOnClickListener {
            selecionarData(data)
        }

        if (agendamentoEditar != null) {

            nome.setText(agendamentoEditar.nomeCliente)
            contato.setText(agendamentoEditar.contato)
            data.setText(agendamentoEditar.data)
            tipo.setText(agendamentoEditar.tipo)
            observacoes.setText(agendamentoEditar.observacoes)
            valor.setText(
                String.format(
                    Locale.US,
                    "%.2f",
                    agendamentoEditar.valorTotal
                )
            )
            sinal.setText(
                String.format(
                    Locale.US,
                    "%.2f",
                    agendamentoEditar.sinal
                )
            )
        } else {
            sinal.setText("0")
        }

        val salvar = botao(
            if (agendamentoEditar == null)
                "SALVAR AGENDAMENTO"
            else
                "SALVAR ALTERAÇÕES"
        ) {

            val nomeTexto = nome.text.toString().trim()
            val contatoTexto = contato.text.toString().trim()
            val dataTexto = data.text.toString().trim()
            val tipoTexto = tipo.text.toString().trim()
            val observacoesTexto =
                observacoes.text.toString().trim()

            val valorTotal =
                valor.text.toString()
                    .replace(",", ".")
                    .toDoubleOrNull()

            val valorSinal =
                sinal.text.toString()
                    .replace(",", ".")
                    .toDoubleOrNull()

            if (
                nomeTexto.isBlank() ||
                contatoTexto.isBlank() ||
                dataTexto.isBlank() ||
                tipoTexto.isBlank() ||
                valorTotal == null ||
                valorSinal == null
            ) {

                Toast.makeText(
                    this,
                    "Preencha todos os campos obrigatórios.",
                    Toast.LENGTH_SHORT
                ).show()

                return@botao
            }

            if (valorSinal > valorTotal) {

                Toast.makeText(
                    this,
                    "O sinal não pode ser maior que o valor total.",
                    Toast.LENGTH_SHORT
                ).show()

                return@botao
            }

            if (agendamentoEditar == null) {

                val clienteId =
                    salvarOuObterCliente(
                        nomeTexto,
                        contatoTexto
                    )

                val valores = ContentValues()

                valores.put("cliente_id", clienteId)
                valores.put("data", dataTexto)
                valores.put("tipo", tipoTexto)
                valores.put(
                    "observacoes",
                    observacoesTexto
                )
                valores.put("valor_total", valorTotal)
                valores.put("sinal", valorSinal)
                valores.put("status", "Agendado")

                banco.writableDatabase.insert(
                    "agendamentos",
                    null,
                    valores
                )

                Toast.makeText(
                    this,
                    "Agendamento salvo.",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                atualizarCliente(
                    agendamentoEditar.clienteId,
                    nomeTexto,
                    contatoTexto
                )

                val valores = ContentValues()

                valores.put("data", dataTexto)
                valores.put("tipo", tipoTexto)
                valores.put(
                    "observacoes",
                    observacoesTexto
                )
                valores.put("valor_total", valorTotal)
                valores.put("sinal", valorSinal)

                banco.writableDatabase.update(
                    "agendamentos",
                    valores,
                    "id = ?",
                    arrayOf(
                        agendamentoEditar.id.toString()
                    )
                )

                Toast.makeText(
                    this,
                    "Agendamento atualizado.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            mostrarMenu()
        }

        layout.addView(salvar)

        layout.addView(
            botao("VOLTAR") {
                mostrarMenu()
            }
        )

        setContentView(layout)
    }

    private fun selecionarData(
        campo: EditText
    ) {

        val calendario = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, ano, mes, dia ->

                campo.setText(
                    String.format(
                        Locale("pt", "BR"),
                        "%02d/%02d/%04d",
                        dia,
                        mes + 1,
                        ano
                    )
                )

            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun salvarOuObterCliente(
        nome: String,
        contato: String
    ): Long {

        val db = banco.writableDatabase

        val cursor = db.query(
            "clientes",
            arrayOf("id"),
            "nome = ? AND contato = ?",
            arrayOf(nome, contato),
            null,
            null,
            null,
            "1"
        )

        if (cursor.moveToFirst()) {

            val id = cursor.getLong(0)

            cursor.close()

            return id
        }

        cursor.close()

        val valores = ContentValues()

        valores.put("nome", nome)
        valores.put("contato", contato)

        return db.insert(
            "clientes",
            null,
            valores
        )
    }

    private fun atualizarCliente(
        id: Long,
        nome: String,
        contato: String
    ) {

        val valores = ContentValues()

        valores.put("nome", nome)
        valores.put("contato", contato)

        banco.writableDatabase.update(
            "clientes",
            valores,
            "id = ?",
            arrayOf(id.toString())
        )
    }

    private fun listarAgendamentos() {

        val layout = baseLayout()

        layout.addView(
            titulo("AGENDAMENTOS")
        )

        val adicionar =
            botao("NOVO AGENDAMENTO") {
                novoAgendamento()
            }

        layout.addView(adicionar)

        val cursor =
            banco.readableDatabase.rawQuery(
                """
                SELECT
                    a.id,
                    a.cliente_id,
                    c.nome,
                    c.contato,
                    a.data,
                    a.tipo,
                    a.observacoes,
                    a.valor_total,
                    a.sinal,
                    a.status
                FROM agendamentos a
                INNER JOIN clientes c
                    ON c.id = a.cliente_id
                ORDER BY a.data
                """.trimIndent(),
                null
            )

        if (!cursor.moveToFirst()) {

            val vazio = TextView(this)
            vazio.text = "Nenhum agendamento cadastrado."
            vazio.setPadding(0, 30, 0, 30)

            layout.addView(vazio)

        } else {

            do {

                val agendamento =
                    Agendamento(
                        id = cursor.getLong(0),
                        clienteId = cursor.getLong(1),
                        nomeCliente = cursor.getString(2),
                        contato = cursor.getString(3),
                        data = cursor.getString(4),
                        tipo = cursor.getString(5),
                        observacoes = cursor.getString(6) ?: "",
                        valorTotal = cursor.getDouble(7),
                        sinal = cursor.getDouble(8),
                        status = cursor.getString(9)
                    )

                layout.addView(
                    criarCardAgendamento(
                        agendamento
                    )
                )

            } while (cursor.moveToNext())
        }

        cursor.close()

        layout.addView(
            botao("VOLTAR") {
                mostrarMenu()
            }
        )

        setContentView(
            ScrollView(this).apply {
                addView(layout)
            }
        )
    }

    private fun criarCardAgendamento(
        agendamento: Agendamento
    ): LinearLayout {

        val card = LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(0, 20, 0, 20)

        val restante =
            agendamento.valorTotal -
                agendamento.sinal

        val texto = TextView(this)

        texto.text =
            """
            ${agendamento.nomeCliente}
            ${agendamento.contato}

            Data: ${agendamento.data}
            Tipo: ${agendamento.tipo}

            Valor: R$ ${
                String.format(
                    Locale("pt", "BR"),
                    "%.2f",
                    agendamento.valorTotal
                )
            }

            Sinal: R$ ${
                String.format(
                    Locale("pt", "BR"),
                    "%.2f",
                    agendamento.sinal
                )
            }

            Restante: R$ ${
                String.format(
                    Locale("pt", "BR"),
                    "%.2f",
                    restante
                )
            }

            Status: ${agendamento.status}

            Observações:
            ${agendamento.observacoes}
            """.trimIndent()

        texto.textSize = 16f

        card.addView(texto)

        card.addView(
            botao("EDITAR / REAGENDAR") {
                novoAgendamento(agendamento)
            }
        )

        card.addView(
            botao("MARCAR COMO REALIZADO") {

                val valores = ContentValues()

                valores.put(
                    "status",
                    "Realizado"
                )

                banco.writableDatabase.update(
                    "agendamentos",
                    valores,
                    "id = ?",
                    arrayOf(
                        agendamento.id.toString()
                    )
                )

                criarEntrega(agendamento)

                listarAgendamentos()
            }
        )

        return card
    }

    private fun criarEntrega(
        agendamento: Agendamento
    ) {

        val existente =
            banco.readableDatabase.rawQuery(
                """
                SELECT id
                FROM entregas
                WHERE agendamento_id = ?
                """.trimIndent(),
                arrayOf(
                    agendamento.id.toString()
                )
            )

        val jaExiste = existente.moveToFirst()

        existente.close()

        if (jaExiste) {
            return
        }

        val calendario = Calendar.getInstance()

        // Prazo padrão inicial: 7 dias após o ensaio.
        // Poderemos transformar isso em configuração posteriormente.
        try {

            val dataEnsaio =
                formatoData.parse(
                    agendamento.data
                )

            if (dataEnsaio != null) {

                calendario.time = dataEnsaio
                calendario.add(
                    Calendar.DAY_OF_MONTH,
                    7
                )

                val dataEntrega =
                    formatoData.format(
                        calendario.time
                    )

                val valores = ContentValues()

                valores.put(
                    "agendamento_id",
                    agendamento.id
                )

                valores.put(
                    "data_entrega",
                    dataEntrega
                )

                valores.put(
                    "status",
                    "Aguardando tratamento"
                )

                banco.writableDatabase.insert(
                    "entregas",
                    null,
                    valores
                )
            }

        } catch (_: Exception) {
        }
    }

    private fun listarEntregas() {

        val layout = baseLayout()

        layout.addView(
            titulo("ENTREGAS")
        )

        val cursor =
            banco.readableDatabase.rawQuery(
                """
                SELECT
                    e.id,
                    e.agendamento_id,
                    c.nome,
                    a.data,
                    e.data_entrega,
                    e.status
                FROM entregas e
                INNER JOIN agendamentos a
                    ON a.id = e.agendamento_id
                INNER JOIN clientes c
                    ON c.id = a.cliente_id
                ORDER BY e.data_entrega
                """.trimIndent(),
                null
            )

        if (!cursor.moveToFirst()) {

            val vazio = TextView(this)

            vazio.text =
                "Nenhuma entrega cadastrada."

            vazio.setPadding(
                0,
                30,
                0,
                30
            )

            layout.addView(vazio)

        } else {

            do {

                val entrega =
                    Entrega(
                        id = cursor.getLong(0),
                        agendamentoId = cursor.getLong(1),
                        nomeCliente = cursor.getString(2),
                        dataEnsaio = cursor.getString(3),
                        dataEntrega = cursor.getString(4),
                        status = cursor.getString(5)
                    )

                layout.addView(
                    criarCardEntrega(
                        entrega
                    )
                )

            } while (cursor.moveToNext())
        }

        cursor.close()

        layout.addView(
            botao("VOLTAR") {
                mostrarMenu()
            }
        )

        setContentView(
            ScrollView(this).apply {
                addView(layout)
            }
        )
    }

    private fun criarCardEntrega(
        entrega: Entrega
    ): LinearLayout {

        val card = LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            0,
            20,
            0,
            20
        )

        val dias =
            calcularDiasRestantes(
                entrega.dataEntrega
            )

        val indicador =
            when {
                entrega.status == "Entregue" ->
                    "ENTREGUE"

                dias < 0 ->
                    "ATRASADA — ${-dias} dia(s)"

                dias == 0L ->
                    "VENCE HOJE"

                dias <= 2 ->
                    "PRAZO VENCENDO — $dias dia(s)"

                dias <= 4 ->
                    "PRAZO PRÓXIMO — $dias dia(s)"

                else ->
                    "PRAZO CONFORTÁVEL — $dias dia(s)"
            }

        val texto = TextView(this)

        texto.text =
            """
            ${entrega.nomeCliente}

            Ensaio: ${entrega.dataEnsaio}
            Entrega: ${entrega.dataEntrega}

            $indicador

            Status: ${entrega.status}
            """.trimIndent()

        texto.textSize = 16f

        card.addView(texto)

        if (entrega.status != "Entregue") {

            card.addView(
                botao("AGUARDANDO TRATAMENTO") {
                    alterarStatusEntrega(
                        entrega.id,
                        "Aguardando tratamento"
                    )
                }
            )

            card.addView(
                botao("EM TRATAMENTO") {
                    alterarStatusEntrega(
                        entrega.id,
                        "Em tratamento"
                    )
                }
            )

            card.addView(
                botao("PRONTA") {
                    alterarStatusEntrega(
                        entrega.id,
                        "Pronta"
                    )
                }
            )

            card.addView(
                botao("MARCAR COMO ENTREGUE") {
                    alterarStatusEntrega(
                        entrega.id,
                        "Entregue"
                    )
                }
            )
        }

        return card
    }

    private fun alterarStatusEntrega(
        id: Long,
        status: String
    ) {

        val valores = ContentValues()

        valores.put(
            "status",
            status
        )

        banco.writableDatabase.update(
            "entregas",
            valores,
            "id = ?",
            arrayOf(id.toString())
        )

        listarEntregas()
    }

    private fun calcularDiasRestantes(
        dataEntrega: String
    ): Long {

        return try {

            val data =
                formatoData.parse(
                    dataEntrega
                ) ?: return 0

            val hoje =
                Calendar.getInstance()

            zerarHorario(hoje)

            val entrega =
                Calendar.getInstance()

            entrega.time = data

            zerarHorario(entrega)

            TimeUnit.MILLISECONDS.toDays(
                entrega.timeInMillis -
                    hoje.timeInMillis
            )

        } catch (_: Exception) {
            0
        }
    }

    private fun zerarHorario(
        calendario: Calendar
    ) {
        calendario.set(
            Calendar.HOUR_OF_DAY,
            0
        )
        calendario.set(
            Calendar.MINUTE,
            0
        )
        calendario.set(
            Calendar.SECOND,
            0
        )
        calendario.set(
            Calendar.MILLISECOND,
            0
        )
    }
}
