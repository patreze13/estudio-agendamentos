package com.patreze.estudioagendamentos

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class Agendamento(
    val id: Long,
    val clienteId: Long,
    val nomeCliente: String,
    val contato: String,
    val data: String,
    val horario: String,
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
    val horario: String,
    val dataEntrega: String,
    val status: String
)

class BancoHelper(context: android.content.Context) :
    SQLiteOpenHelper(context, "estudio_agendamentos.db", null, 2) {

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
                horario TEXT NOT NULL,
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

    private fun layoutBase(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }
    }

    private fun titulo(texto: String): TextView {

        return TextView(this).apply {
            text = texto
            textSize = 25f
            setPadding(0, 0, 0, 25)
        }
    }

    private fun campo(
        dica: String
    ): EditText {

        return EditText(this).apply {
            hint = dica
            textSize = 16f
            setPadding(10, 10, 10, 10)
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

        val layout = layoutBase()

        layout.gravity = Gravity.CENTER_HORIZONTAL

        layout.addView(
            titulo("ESTÚDIO")
        )

        layout.addView(
            botao("NOVO AGENDAMENTO") {
                novoAgendamento()
            }
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

        setContentView(layout)
    }

    private fun novoAgendamento(
        editar: Agendamento? = null
    ) {

        val layout = layoutBase()

        layout.addView(
            titulo(
                if (editar == null)
                    "NOVO AGENDAMENTO"
                else
                    "EDITAR AGENDAMENTO"
            )
        )

        val nome = campo(
            "Nome do cliente"
        )

        val contato = campo(
            "Contato / WhatsApp"
        )

        val data = campo(
            "Data do ensaio — DD/MM/AAAA"
        )

        val horario = campo(
            "Horário do ensaio — HH:MM"
        )

        val tipo = campo(
            "Tipo de ensaio"
        )

        val observacoes = campo(
            "Observações"
        )

        observacoes.minLines = 4
        observacoes.gravity = Gravity.TOP

        val valorTotal = campo(
            "Valor total — exemplo: 300,00"
        )

        val sinal = campo(
            "Valor do sinal — deixe 0 se não houver"
        )

        layout.addView(nome)
        layout.addView(contato)
        layout.addView(data)
        layout.addView(horario)
        layout.addView(tipo)
        layout.addView(observacoes)
        layout.addView(valorTotal)
        layout.addView(sinal)

        data.isFocusable = false
        horario.isFocusable = false

        data.setOnClickListener {
            selecionarData(data)
        }

        horario.setOnClickListener {
            selecionarHorario(horario)
        }

        if (editar != null) {

            nome.setText(editar.nomeCliente)
            contato.setText(editar.contato)
            data.setText(editar.data)
            horario.setText(editar.horario)
            tipo.setText(editar.tipo)
            observacoes.setText(editar.observacoes)

            valorTotal.setText(
                String.format(
                    Locale.US,
                    "%.2f",
                    editar.valorTotal
                )
            )

            sinal.setText(
                String.format(
                    Locale.US,
                    "%.2f",
                    editar.sinal
                )
            )

        } else {
            sinal.setText("0")
        }

        layout.addView(
            botao(
                if (editar == null)
                    "SALVAR AGENDAMENTO"
                else
                    "SALVAR ALTERAÇÕES"
            ) {

                val nomeTexto =
                    nome.text.toString().trim()

                val contatoTexto =
                    contato.text.toString().trim()

                val dataTexto =
                    data.text.toString().trim()

                val horarioTexto =
                    horario.text.toString().trim()

                val tipoTexto =
                    tipo.text.toString().trim()

                val observacoesTexto =
                    observacoes.text.toString().trim()

                val total =
                    converterValor(
                        valorTotal.text.toString()
                    )

                val valorSinal =
                    converterValor(
                        sinal.text.toString()
                    ) ?: 0.0

                if (
                    nomeTexto.isBlank() ||
                    contatoTexto.isBlank() ||
                    dataTexto.isBlank() ||
                    horarioTexto.isBlank() ||
                    tipoTexto.isBlank() ||
                    total == null
                ) {

                    Toast.makeText(
                        this,
                        "Preencha os campos obrigatórios.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@botao
                }

                if (valorSinal > total) {

                    Toast.makeText(
                        this,
                        "O sinal não pode ser maior que o valor total.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@botao
                }

                if (editar == null) {

                    val clienteId =
                        salvarOuObterCliente(
                            nomeTexto,
                            contatoTexto
                        )

                    val valores =
                        ContentValues()

                    valores.put(
                        "cliente_id",
                        clienteId
                    )

                    valores.put(
                        "data",
                        dataTexto
                    )

                    valores.put(
                        "horario",
                        horarioTexto
                    )

                    valores.put(
                        "tipo",
                        tipoTexto
                    )

                    valores.put(
                        "observacoes",
                        observacoesTexto
                    )

                    valores.put(
                        "valor_total",
                        total
                    )

                    valores.put(
                        "sinal",
                        valorSinal
                    )

                    valores.put(
                        "status",
                        "Agendado"
                    )

                    banco.writableDatabase.insert(
                        "agendamentos",
                        null,
                        valores
                    )

                    Toast.makeText(
                        this,
                        "Agendamento salvo com sucesso.",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    atualizarCliente(
                        editar.clienteId,
                        nomeTexto,
                        contatoTexto
                    )

                    val valores =
                        ContentValues()

                    valores.put(
                        "data",
                        dataTexto
                    )

                    valores.put(
                        "horario",
                        horarioTexto
                    )

                    valores.put(
                        "tipo",
                        tipoTexto
                    )

                    valores.put(
                        "observacoes",
                        observacoesTexto
                    )

                    valores.put(
                        "valor_total",
                        total
                    )

                    valores.put(
                        "sinal",
                        valorSinal
                    )

                    banco.writableDatabase.update(
                        "agendamentos",
                        valores,
                        "id = ?",
                        arrayOf(
                            editar.id.toString()
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
        )

        layout.addView(
            botao("CANCELAR") {
                mostrarMenu()
            }
        )

        setContentView(
            ScrollView(this).apply {
                addView(layout)
            }
        )
    }

    private fun converterValor(
        texto: String
    ): Double? {

        val limpo =
            texto.trim()
                .replace("R$", "")
                .replace(" ", "")

        if (limpo.isBlank()) {
            return null
        }

        return try {

            if (
                limpo.contains(",") &&
                limpo.contains(".")
            ) {

                limpo
                    .replace(".", "")
                    .replace(",", ".")
                    .toDouble()

            } else {

                limpo
                    .replace(",", ".")
                    .toDouble()
            }

        } catch (_: Exception) {
            null
        }
    }

    private fun selecionarData(
        campo: EditText
    ) {

        val calendario =
            Calendar.getInstance()

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
            calendario.get(
                Calendar.YEAR
            ),
            calendario.get(
                Calendar.MONTH
            ),
            calendario.get(
                Calendar.DAY_OF_MONTH
            )
        ).show()
    }

    private fun selecionarHorario(
        campo: EditText
    ) {

        val calendario =
            Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hora, minuto ->

                campo.setText(
                    String.format(
                        Locale("pt", "BR"),
                        "%02d:%02d",
                        hora,
                        minuto
                    )
                )

            },
            calendario.get(
                Calendar.HOUR_OF_DAY
            ),
            calendario.get(
                Calendar.MINUTE
            ),
            true
        ).show()
    }

    private fun salvarOuObterCliente(
        nome: String,
        contato: String
    ): Long {

        val db =
            banco.writableDatabase

        val cursor =
            db.query(
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

            val id =
                cursor.getLong(0)

            cursor.close()

            return id
        }

        cursor.close()

        val valores =
            ContentValues()

        valores.put(
            "nome",
            nome
        )

        valores.put(
            "contato",
            contato
        )

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

        val valores =
            ContentValues()

        valores.put(
            "nome",
            nome
        )

        valores.put(
            "contato",
            contato
        )

        banco.writableDatabase.update(
            "clientes",
            valores,
            "id = ?",
            arrayOf(id.toString())
        )
    }

    private fun listarAgendamentos() {

        val layout =
            layoutBase()

        layout.addView(
            titulo("AGENDAMENTOS")
        )

        layout.addView(
            botao("NOVO AGENDAMENTO") {
                novoAgendamento()
            }
        )

        val cursor =
            banco.readableDatabase.rawQuery(
                """
                SELECT
                    a.id,
                    a.cliente_id,
                    c.nome,
                    c.contato,
                    a.data,
                    a.horario,
                    a.tipo,
                    a.observacoes,
                    a.valor_total,
                    a.sinal,
                    a.status
                FROM agendamentos a
                INNER JOIN clientes c
                    ON c.id = a.cliente_id
                ORDER BY
                    a.data ASC,
                    a.horario ASC
                """.trimIndent(),
                null
            )

        if (!cursor.moveToFirst()) {

            val vazio =
                TextView(this)

            vazio.text =
                "Nenhum agendamento cadastrado."

            vazio.setPadding(
                0,
                30,
                0,
                30
            )

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
                        horario = cursor.getString(5),
                        tipo = cursor.getString(6),
                        observacoes =
                            cursor.getString(7) ?: "",
                        valorTotal =
                            cursor.getDouble(8),
                        sinal =
                            cursor.getDouble(9),
                        status =
                            cursor.getString(10)
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

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            0,
            20,
            0,
            20
        )

        val restante =
            agendamento.valorTotal -
                agendamento.sinal

        val texto =
            TextView(this)

        texto.text =
            """
            ${agendamento.nomeCliente}
            ${agendamento.contato}

            Data: ${agendamento.data}
            Horário: ${agendamento.horario}
            Tipo: ${agendamento.tipo}

            Valor total: R$ ${
                formatarDinheiro(
                    agendamento.valorTotal
                )
            }

            Sinal: R$ ${
                formatarDinheiro(
                    agendamento.sinal
                )
            }

            Restante: R$ ${
                formatarDinheiro(
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
                novoAgendamento(
                    agendamento
                )
            }
        )

        if (agendamento.status != "Realizado") {

            card.addView(
                botao("MARCAR COMO REALIZADO") {

                    marcarComoRealizado(
                        agendamento
                    )

                    listarAgendamentos()
                }
            )
        }

        return card
    }

    private fun marcarComoRealizado(
        agendamento: Agendamento
    ) {

        val valores =
            ContentValues()

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

        criarEntrega(
            agendamento
        )
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

        val jaExiste =
            existente.moveToFirst()

        existente.close()

        if (jaExiste) {
            return
        }

        try {

            val dataEnsaio =
                formatoData.parse(
                    agendamento.data
                ) ?: return

            val calendario =
                Calendar.getInstance()

            calendario.time =
                dataEnsaio

            calendario.add(
                Calendar.DAY_OF_MONTH,
                7
            )

            val dataEntrega =
                formatoData.format(
                    calendario.time
                )

            val valores =
                ContentValues()

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

        } catch (_: Exception) {
        }
    }

    private fun listarEntregas() {

        val layout =
            layoutBase()

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
                    a.horario,
                    e.data_entrega,
                    e.status
                FROM entregas e
                INNER JOIN agendamentos a
                    ON a.id = e.agendamento_id
                INNER JOIN clientes c
                    ON c.id = a.cliente_id
                ORDER BY e.data_entrega ASC
                """.trimIndent(),
                null
            )

        if (!cursor.moveToFirst()) {

            val vazio =
                TextView(this)

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
                        agendamentoId =
                            cursor.getLong(1),
                        nomeCliente =
                            cursor.getString(2),
                        dataEnsaio =
                            cursor.getString(3),
                        horario =
                            cursor.getString(4),
                        dataEntrega =
                            cursor.getString(5),
                        status =
                            cursor.getString(6)
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

        val card =
            LinearLayout(this)

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

                dias < 0L ->
                    "ATRASADA — ${-dias} dia(s)"

                dias == 0L ->
                    "VENCE HOJE"

                dias <= 2L ->
                    "PRAZO VENCENDO — $dias dia(s)"

                dias <= 4L ->
                    "PRAZO PRÓXIMO — $dias dia(s)"

                else ->
                    "PRAZO CONFORTÁVEL — $dias dia(s)"
            }

        val texto =
            TextView(this)

        texto.text =
            """
            ${entrega.nomeCliente}

            Ensaio: ${entrega.dataEnsaio}
            Horário: ${entrega.horario}

            Data limite:
            ${entrega.dataEntrega}

            $indicador

            Status: ${entrega.status}
            """.trimIndent()

        texto.textSize = 16f

        card.addView(texto)

        card.addView(
            botao("ALTERAR DATA DE ENTREGA") {

                selecionarNovaDataEntrega(
                    entrega
                )
            }
        )

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

    private fun selecionarNovaDataEntrega(
        entrega: Entrega
    ) {

        val calendario =
            Calendar.getInstance()

        try {

            calendario.time =
                formatoData.parse(
                    entrega.dataEntrega
                ) ?: calendario.time

        } catch (_: Exception) {
        }

        DatePickerDialog(
            this,
            { _, ano, mes, dia ->

                val novaData =
                    String.format(
                        Locale("pt", "BR"),
                        "%02d/%02d/%04d",
                        dia,
                        mes + 1,
                        ano
                    )

                val valores =
                    ContentValues()

                valores.put(
                    "data_entrega",
                    novaData
                )

                banco.writableDatabase.update(
                    "entregas",
                    valores,
                    "id = ?",
                    arrayOf(
                        entrega.id.toString()
                    )
                )

                listarEntregas()

            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun alterarStatusEntrega(
        id: Long,
        status: String
    ) {

        val valores =
            ContentValues()

        valores.put(
            "status",
            status
        )

        banco.writableDatabase.update(
            "entregas",
            valores,
            "id = ?",
            arrayOf(
                id.toString()
            )
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
                ) ?: return 0L

            val hoje =
                Calendar.getInstance()

            zerarHorario(hoje)

            val entrega =
                Calendar.getInstance()

            entrega.time =
                data

            zerarHorario(entrega)

            TimeUnit.MILLISECONDS.toDays(
                entrega.timeInMillis -
                    hoje.timeInMillis
            )

        } catch (_: Exception) {

            0L
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

    private fun formatarDinheiro(
        valor: Double
    ): String {

        return String.format(
            Locale("pt", "BR"),
            "%.2f",
            valor
        )
    }
}
