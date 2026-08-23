package com.patreze.estudioagendamentos

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class Agendamento(
    val id: Long,
    val nome: String,
    val contato: String,
    val data: String,
    val horario: String,
    val tipo: String,
    val observacoes: String,
    val valor: Double,
    val sinal: Double,
    val realizado: Boolean,
    val prazoEntrega: String?,
    val entregaRealizada: Boolean
)

class Banco(activity: Activity) : SQLiteOpenHelper(
    activity,
    "estudio.db",
    null,
    2
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE agendamentos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT,
                contato TEXT,
                data TEXT,
                horario TEXT,
                tipo TEXT,
                observacoes TEXT,
                valor REAL,
                sinal REAL,
                realizado INTEGER DEFAULT 0,
                prazo_entrega TEXT,
                entrega_realizada INTEGER DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        antiga: Int,
        nova: Int
    ) {
        if (antiga < 2) {
            db.execSQL(
                "ALTER TABLE agendamentos ADD COLUMN realizado INTEGER DEFAULT 0"
            )

            db.execSQL(
                "ALTER TABLE agendamentos ADD COLUMN prazo_entrega TEXT"
            )

            db.execSQL(
                "ALTER TABLE agendamentos ADD COLUMN entrega_realizada INTEGER DEFAULT 0"
            )
        }
    }
}

class MainActivity : Activity() {

    private lateinit var banco: Banco

    private val lilasClaro = Color.rgb(232, 220, 245)
    private val lilasFundo = Color.rgb(238, 228, 248)

    private var telaAtual = "inicio"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        banco = Banco(this)

        telaInicial()
    }

    override fun onBackPressed() {
        if (telaAtual != "inicio") {
            telaInicial()
        } else {
            super.onBackPressed()
        }
    }

    private fun fundo(cor: Int): FrameLayout {
        return FrameLayout(this).apply {
            setBackgroundColor(cor)
        }
    }

    private fun tituloPrincipal(): TextView {
        return TextView(this).apply {
            text = "Agendamentos\nEstúdio Rafa Fraga"
            textSize = 30f
            gravity = Gravity.CENTER
            typeface = Typeface.create("cursive", Typeface.NORMAL)
            setTextColor(Color.rgb(80, 55, 90))
            setPadding(0, 0, 0, 35)
        }
    }

    private fun tituloInterno(texto: String): TextView {
        return TextView(this).apply {
            text = texto
            textSize = 26f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(70, 55, 75))
            setPadding(0, 0, 0, 25)
        }
    }

    private fun botao(
        texto: String,
        interno: Boolean = false,
        acao: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = texto
            textSize = 15f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD

            background = GradientDrawable().apply {
                cornerRadius = 22f
                setColor(
                    if (interno) lilasClaro else Color.WHITE
                )
            }

            elevation = 5f
            isClickable = true
            isFocusable = true

            setOnClickListener {
                acao()
            }

            layoutParams =
                LinearLayout.LayoutParams(
                    620,
                    65
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
        }
    }

    private fun campo(dica: String): EditText {
        return EditText(this).apply {
            hint = dica
            textSize = 14f
            setTextColor(Color.BLACK)
            setHintTextColor(Color.rgb(150, 150, 150))

            setPadding(18, 8, 18, 8)

            background = GradientDrawable().apply {
                cornerRadius = 18f
                setColor(Color.rgb(248, 248, 248))
                setStroke(
                    2,
                    Color.rgb(225, 225, 225)
                )
            }

            layoutParams =
                LinearLayout.LayoutParams(
                    -1,
                    60
                ).apply {
                    setMargins(0, 0, 0, 10)
                }
        }
    }

    private fun telaInicial() {

        telaAtual = "inicio"

        val tela = fundo(lilasFundo)

        val conteudo =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
            }

        conteudo.addView(
            tituloPrincipal(),
            LinearLayout.LayoutParams(-1, -2)
        )

        conteudo.addView(
            botao("NOVO AGENDAMENTO") {
                novoAgendamento()
            }
        )

        conteudo.addView(
            botao("AGENDAMENTOS") {
                listarAgendamentos()
            }
        )

        conteudo.addView(
            botao("ENTREGAS") {
                telaEntregas()
            }
        )

        tela.addView(
            conteudo,
            FrameLayout.LayoutParams(
                -1,
                -2,
                Gravity.CENTER
            ).apply {
                leftMargin = 40
                rightMargin = 40
            }
        )

        setContentView(tela)

        window.statusBarColor = lilasFundo
        window.navigationBarColor = lilasFundo
    }

    private fun novoAgendamento(
        editar: Agendamento? = null
    ) {

        telaAtual = "agendamento"

        val tela = fundo(Color.WHITE)

        val conteudo =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(30, 20, 30, 20)
            }

        conteudo.addView(
            tituloInterno(
                if (editar == null)
                    "NOVO AGENDAMENTO"
                else
                    "EDITAR AGENDAMENTO"
            )
        )

        val nome = campo("Nome do Cliente")

        val contato = campo("Contato")

        aplicarMascaraTelefone(contato)

        val data = campo("Data do ensaio")

        data.isFocusable = false
        data.isClickable = true

        data.setOnClickListener {
            escolherData(data)
        }

        val horario = campo("Horário do ensaio")

        horario.isFocusable = false
        horario.isClickable = true

        horario.setOnClickListener {
            escolherHorario(horario)
        }

        val tipo = campo("Tipo de ensaio")

        val observacoes = campo("Observações")

        observacoes.minLines = 3

        val valor =
            campo(
                "Valor total do ensaio — somente o número"
            )

        valor.inputType =
            InputType.TYPE_CLASS_NUMBER

        val sinal =
            campo(
                "Valor do SINAL — somente o número"
            )

        sinal.inputType =
            InputType.TYPE_CLASS_NUMBER

        if (editar != null) {
            nome.setText(editar.nome)
            contato.setText(editar.contato)
            data.setText(editar.data)
            horario.setText(editar.horario)
            tipo.setText(editar.tipo)
            observacoes.setText(editar.observacoes)

            if (editar.valor > 0) {
                valor.setText(
                    editar.valor.toInt().toString()
                )
            }

            if (editar.sinal > 0) {
                sinal.setText(
                    editar.sinal.toInt().toString()
                )
            }
        }

        conteudo.addView(nome)
        conteudo.addView(contato)
        conteudo.addView(data)
        conteudo.addView(horario)
        conteudo.addView(tipo)
        conteudo.addView(observacoes)
        conteudo.addView(valor)
        conteudo.addView(sinal)

        conteudo.addView(
            botao(
                if (editar == null)
                    "SALVAR AGENDAMENTO"
                else
                    "SALVAR ALTERAÇÕES",
                true
            ) {
                salvarAgendamento(
                    editar,
                    nome.text.toString().trim(),
                    contato.text.toString().trim(),
                    data.text.toString().trim(),
                    horario.text.toString().trim(),
                    tipo.text.toString().trim(),
                    observacoes.text.toString().trim(),
                    valor.text.toString().trim(),
                    sinal.text.toString().trim()
                )
            }
        )

        /*
         * NOVO:
         * Quando estiver editando um agendamento ainda não realizado,
         * aparece a opção para registrar que o ensaio aconteceu.
         */
        if (editar != null && !editar.realizado) {

            conteudo.addView(
                botao(
                    "ENSAIO REALIZADO",
                    true
                ) {
                    marcarEnsaioRealizado(editar.id)
                }
            )
        }

        conteudo.addView(
            botao(
                "VOLTAR",
                true
            ) {
                telaInicial()
            }
        )

        tela.addView(
            conteudo,
            FrameLayout.LayoutParams(
                -1,
                -2,
                Gravity.CENTER
            )
        )

        setContentView(tela)

        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
    }

    private fun salvarAgendamento(
        editar: Agendamento?,
        nome: String,
        contato: String,
        data: String,
        horario: String,
        tipo: String,
        observacoes: String,
        valorTexto: String,
        sinalTexto: String
    ) {

        val valor =
            valorTexto.toDoubleOrNull() ?: 0.0

        val sinal =
            sinalTexto.toDoubleOrNull() ?: 0.0

        val dados = ContentValues()

        dados.put("nome", nome)
        dados.put("contato", contato)
        dados.put("data", data)
        dados.put("horario", horario)
        dados.put("tipo", tipo)
        dados.put("observacoes", observacoes)
        dados.put("valor", valor)
        dados.put("sinal", sinal)

        if (editar == null) {

            banco.writableDatabase.insert(
                "agendamentos",
                null,
                dados
            )

            Toast.makeText(
                this,
                "Agendamento cadastrado com sucesso.",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            banco.writableDatabase.update(
                "agendamentos",
                dados,
                "id = ?",
                arrayOf(editar.id.toString())
            )

            Toast.makeText(
                this,
                "Agendamento atualizado.",
                Toast.LENGTH_SHORT
            ).show()
        }

        listarAgendamentos()
    }

    private fun marcarEnsaioRealizado(id: Long) {

        val cursor =
            banco.readableDatabase.rawQuery(
                "SELECT data FROM agendamentos WHERE id = ?",
                arrayOf(id.toString())
            )

        var dataEnsaio = ""

        if (cursor.moveToFirst()) {
            dataEnsaio =
                cursor.getString(0) ?: ""
        }

        cursor.close()

        val prazo =
            calcularPrazo(dataEnsaio)

        val dados = ContentValues()

        dados.put("realizado", 1)
        dados.put("prazo_entrega", prazo)
        dados.put("entrega_realizada", 0)

        banco.writableDatabase.update(
            "agendamentos",
            dados,
            "id = ?",
            arrayOf(id.toString())
        )

        Toast.makeText(
            this,
            "Ensaio marcado como realizado. Prazo: $prazo",
            Toast.LENGTH_LONG
        ).show()

        listarAgendamentos()
    }

    private fun calcularPrazo(
        data: String
    ): String {

        return try {

            val formato =
                SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale("pt", "BR")
                )

            val dataOriginal =
                formato.parse(data)

            val calendario =
                Calendar.getInstance()

            calendario.time = dataOriginal!!

            calendario.add(
                Calendar.DAY_OF_MONTH,
                7
            )

            formato.format(
                calendario.time
            )

        } catch (_: Exception) {

            ""
        }
    }

    private fun aplicarMascaraTelefone(
        campo: EditText
    ) {

        var alterando = false

        campo.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {}

                override fun afterTextChanged(
                    s: Editable?
                ) {

                    if (alterando)
                        return

                    val numeros =
                        s.toString()
                            .filter { it.isDigit() }
                            .take(11)

                    if (numeros.isEmpty())
                        return

                    val formatado =
                        when {

                            numeros.length <= 2 ->
                                "($numeros"

                            numeros.length <= 7 ->
                                "(${numeros.substring(0, 2)}) " +
                                    numeros.substring(2)

                            else ->
                                "(${numeros.substring(0, 2)}) " +
                                    numeros.substring(2, 7) +
                                    "-" +
                                    numeros.substring(7)
                        }

                    if (
                        formatado != s.toString()
                    ) {

                        alterando = true

                        campo.setText(formatado)

                        campo.setSelection(
                            formatado.length
                        )

                        alterando = false
                    }
                }
            }
        )
    }

    private fun escolherData(
        campo: EditText
    ) {

        val agora =
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
            agora.get(Calendar.YEAR),
            agora.get(Calendar.MONTH),
            agora.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun escolherHorario(
        campo: EditText
    ) {

        val agora =
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
            agora.get(Calendar.HOUR_OF_DAY),
            agora.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun listarAgendamentos() {

        telaAtual = "agendamentos"

        val tela = fundo(Color.WHITE)

        val conteudo =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(35, 25, 35, 25)
            }

        conteudo.addView(
            tituloInterno("AGENDAMENTOS")
        )

        conteudo.addView(
            botao(
                "NOVO AGENDAMENTO",
                true
            ) {
                novoAgendamento()
            }
        )

        val cursor =
            banco.readableDatabase.rawQuery(
                """
                SELECT
                    id,
                    nome,
                    contato,
                    data,
                    horario,
                    tipo,
                    observacoes,
                    valor,
                    sinal,
                    realizado,
                    prazo_entrega,
                    entrega_realizada
                FROM agendamentos
                ORDER BY data, horario
                """.trimIndent(),
                null
            )

        if (!cursor.moveToFirst()) {

            val aviso = TextView(this)

            aviso.text =
                "Nenhum agendamento cadastrado."

            aviso.textSize = 17f
            aviso.gravity = Gravity.CENTER

            aviso.setPadding(
                0,
                25,
                0,
                25
            )

            conteudo.addView(aviso)

        } else {

            do {

                val agendamento =
                    Agendamento(
                        cursor.getLong(0),
                        cursor.getString(1) ?: "",
                        cursor.getString(2) ?: "",
                        cursor.getString(3) ?: "",
                        cursor.getString(4) ?: "",
                        cursor.getString(5) ?: "",
                        cursor.getString(6) ?: "",
                        cursor.getDouble(7),
                        cursor.getDouble(8),
                        cursor.getInt(9) == 1,
                        cursor.getString(10),
                        cursor.getInt(11) == 1
                    )

                conteudo.addView(
                    cardAgendamento(
                        agendamento
                    )
                )

            } while (cursor.moveToNext())
        }

        cursor.close()

        conteudo.addView(
            botao(
                "VOLTAR",
                true
            ) {
                telaInicial()
            }
        )

        tela.addView(
            conteudo,
            FrameLayout.LayoutParams(
                -1,
                -2,
                Gravity.CENTER
            )
        )

        setContentView(tela)

        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
    }

    private fun cardAgendamento(
        agendamento: Agendamento
    ): LinearLayout {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.gravity =
            Gravity.CENTER_HORIZONTAL

        card.setPadding(
            0,
            15,
            0,
            15
        )

        val restante =
            agendamento.valor -
                agendamento.sinal

        val status =
            if (agendamento.realizado)
                "ENSAIO REALIZADO"
            else
                "ENSAIO PENDENTE"

        val texto =
            TextView(this)

        texto.text =
            """
            ${agendamento.nome.ifBlank {
                "Cliente sem nome"
            }}

            Contato: ${
                agendamento.contato.ifBlank {
                    "Não informado"
                }
            }

            Data: ${
                agendamento.data.ifBlank {
                    "Não informada"
                }
            }

            Horário: ${
                agendamento.horario.ifBlank {
                    "Não informado"
                }
            }

            Tipo: ${
                agendamento.tipo.ifBlank {
                    "Não informado"
                }
            }

            Valor: R$ ${formatar(agendamento.valor)}

            Sinal: R$ ${formatar(agendamento.sinal)}

            Restante: R$ ${formatar(restante)}

            Status: $status

            Observações:
            ${
                agendamento.observacoes.ifBlank {
                    "Nenhuma"
                }
            }
            """.trimIndent()

        texto.textSize = 16f
        texto.gravity = Gravity.CENTER

        card.addView(texto)

        card.addView(
            botao(
                "EDITAR / REAGENDAR",
                true
            ) {
                novoAgendamento(agendamento)
            }
        )

        if (!agendamento.realizado) {

            card.addView(
                botao(
                    "ENSAIO REALIZADO",
                    true
                ) {
                    marcarEnsaioRealizado(
                        agendamento.id
                    )
                }
            )
        }

        return card
    }

    private fun telaEntregas() {

        telaAtual = "entregas"

        val tela = fundo(Color.WHITE)

        val conteudo =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(35, 25, 35, 25)
            }

        conteudo.addView(
            tituloInterno("ENTREGAS")
        )

        val cursor =
            banco.readableDatabase.rawQuery(
                """
                SELECT
                    id,
                    nome,
                    contato,
                    data,
                    horario,
                    tipo,
                    observacoes,
                    valor,
                    sinal,
                    realizado,
                    prazo_entrega,
                    entrega_realizada
                FROM agendamentos
                WHERE realizado = 1
                ORDER BY entrega_realizada, prazo_entrega
                """.trimIndent(),
                null
            )

        if (!cursor.moveToFirst()) {

            val aviso = TextView(this)

            aviso.text =
                "Nenhuma entrega pendente."

            aviso.textSize = 17f
            aviso.gravity = Gravity.CENTER

            aviso.setPadding(
                0,
                25,
                0,
                25
            )

            conteudo.addView(aviso)

        } else {

            do {

                val agendamento =
                    Agendamento(
                        cursor.getLong(0),
                        cursor.getString(1) ?: "",
                        cursor.getString(2) ?: "",
                        cursor.getString(3) ?: "",
                        cursor.getString(4) ?: "",
                        cursor.getString(5) ?: "",
                        cursor.getString(6) ?: "",
                        cursor.getDouble(7),
                        cursor.getDouble(8),
                        cursor.getInt(9) == 1,
                        cursor.getString(10),
                        cursor.getInt(11) == 1
                    )

                conteudo.addView(
                    cardEntrega(agendamento)
                )

            } while (cursor.moveToNext())
        }

        cursor.close()

        conteudo.addView(
            botao(
                "VOLTAR",
                true
            ) {
                telaInicial()
            }
        )

        tela.addView(
            conteudo,
            FrameLayout.LayoutParams(
                -1,
                -2,
                Gravity.CENTER
            )
        )

        setContentView(tela)

        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
    }

    private fun cardEntrega(
        agendamento: Agendamento
    ): LinearLayout {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.gravity =
            Gravity.CENTER_HORIZONTAL

        card.setPadding(
            0,
            15,
            0,
            15
        )

        val prazo =
            agendamento.prazoEntrega
                ?: "Não definido"

        val texto =
            TextView(this)

        texto.text =
            """
            ${agendamento.nome.ifBlank {
                "Cliente sem nome"
            }}

            Ensaio: ${
                agendamento.data.ifBlank {
                    "Não informado"
                }
            }

            Prazo de entrega: $prazo

            Status: ${
                if (agendamento.entregaRealizada)
                    "ENTREGA REALIZADA"
                else
                    "ENTREGA PENDENTE"
            }
            """.trimIndent()

        texto.textSize = 16f
        texto.gravity = Gravity.CENTER

        card.addView(texto)

        if (!agendamento.entregaRealizada) {

            card.addView(
                botao(
                    "ALTERAR PRAZO",
                    true
                ) {
                    alterarPrazo(
                        agendamento.id
                    )
                }
            )

            card.addView(
                botao(
                    "ENTREGA REALIZADA",
                    true
                ) {
                    marcarEntregaRealizada(
                        agendamento.id
                    )
                }
            )
        }

        return card
    }

    private fun alterarPrazo(id: Long) {

        val calendario =
            Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, ano, mes, dia ->

                val data =
                    String.format(
                        Locale("pt", "BR"),
                        "%02d/%02d/%04d",
                        dia,
                        mes + 1,
                        ano
                    )

                val dados =
                    ContentValues()

                dados.put(
                    "prazo_entrega",
                    data
                )

                banco.writableDatabase.update(
                    "agendamentos",
                    dados,
                    "id = ?",
                    arrayOf(id.toString())
                )

                telaEntregas()

            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun marcarEntregaRealizada(id: Long) {

        val dados =
            ContentValues()

        dados.put(
            "entrega_realizada",
            1
        )

        banco.writableDatabase.update(
            "agendamentos",
            dados,
            "id = ?",
            arrayOf(id.toString())
        )

        Toast.makeText(
            this,
            "Entrega marcada como realizada.",
            Toast.LENGTH_SHORT
        ).show()

        telaEntregas()
    }

    private fun formatar(
        valor: Double
    ): String {

        return String.format(
            Locale("pt", "BR"),
            "%.2f",
            valor
        )
    }
}
