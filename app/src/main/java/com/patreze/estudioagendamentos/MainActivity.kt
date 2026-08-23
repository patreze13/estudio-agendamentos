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
import android.text.TextWatcher
import android.view.Gravity
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    private val cinzaTexto = Color.rgb(95, 95, 95)

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
            typeface = Typeface.create(
                "cursive",
                Typeface.NORMAL
            )
            setTextColor(Color.rgb(80, 55, 90))
            setPadding(0, 0, 0, 35)
        }
    }

    private fun tituloInterno(texto: String): TextView {
        return TextView(this).apply {
            text = texto
            textSize = 24f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(70, 55, 75))
            setPadding(0, 0, 0, 20)
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

            // Texto dos botões sempre preto
            setTextColor(Color.BLACK)

            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD

            background = GradientDrawable().apply {
                cornerRadius = 22f
                setColor(
                    if (interno) {
                        lilasClaro
                    } else {
                        Color.WHITE
                    }
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
                    setMargins(0, 7, 0, 7)
                }
        }
    }

    private fun campo(
        dica: String
    ): EditText {

        return EditText(this).apply {

            hint = dica
            textSize = 14f

            setTextColor(Color.BLACK)

            setHintTextColor(
                Color.rgb(150, 150, 150)
            )

            setPadding(
                18,
                8,
                18,
                8
            )

            background =
                GradientDrawable().apply {

                    cornerRadius = 18f

                    setColor(
                        Color.rgb(
                            248,
                            248,
                            248
                        )
                    )

                    setStroke(
                        2,
                        Color.rgb(
                            225,
                            225,
                            225
                        )
                    )
                }

            layoutParams =
                LinearLayout.LayoutParams(
                    -1,
                    60
                ).apply {
                    setMargins(
                        0,
                        0,
                        0,
                        9
                    )
                }
        }
    }

    private fun telaInicial() {

        telaAtual = "inicio"

        val tela = fundo(lilasFundo)

        val conteudo =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_HORIZONTAL
            }

        conteudo.addView(
            tituloPrincipal(),
            LinearLayout.LayoutParams(
                -1,
                -2
            )
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

        conteudo.addView(
            botao("ENSAIOS DO MÊS") {
                calendarioMensal(false)
            }
        )

        conteudo.addView(
            botao("ENTREGAS DO MÊS") {
                calendarioMensal(true)
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

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_HORIZONTAL

                setPadding(
                    30,
                    15,
                    30,
                    15
                )
            }

        conteudo.addView(
            tituloInterno(
                if (editar == null)
                    "NOVO AGENDAMENTO"
                else
                    "EDITAR AGENDAMENTO"
            )
        )

        val nome =
            campo("Nome do Cliente")

        val contato =
            campo("Contato")

        aplicarMascaraTelefone(contato)

        val data =
            campo("Data do ensaio")

        data.isFocusable = false
        data.isClickable = true

        data.setOnClickListener {
            escolherData(data)
        }

        val horario =
            campo("Horário do ensaio")

        horario.isFocusable = false
        horario.isClickable = true

        horario.setOnClickListener {
            escolherHorario(horario)
        }

        val tipo =
            campo("Tipo de ensaio")

        val observacoes =
            campo("Observações")

        observacoes.minLines = 3

        val valor =
            campo(
                "Valor total — somente o número"
            )

        val sinal =
            campo(
                "Sinal — somente o número"
            )

        valor.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER

        sinal.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER

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

        if (
            editar != null &&
            !editar.realizado
        ) {

            conteudo.addView(
                botao(
                    "ENSAIO REALIZADO",
                    true
                ) {
                    marcarEnsaioRealizado(
                        editar.id
                    )
                }
            )
        }

        conteudo.addView(
            botao(
                "VOLTAR",
                true
            ) {
                listarAgendamentos()
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
            valorTexto.toDoubleOrNull()
                ?: 0.0

        val sinal =
            sinalTexto.toDoubleOrNull()
                ?: 0.0

        val dados =
            ContentValues()

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
                "Agendamento cadastrado.",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            banco.writableDatabase.update(
                "agendamentos",
                dados,
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

        listarAgendamentos()
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
                            .filter {
                                it.isDigit()
                            }
                            .take(11)

                    if (
                        numeros.isEmpty()
                    )
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
                        formatado !=
                        s.toString()
                    ) {

                        alterando = true

                        campo.setText(
                            formatado
                        )

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

    private fun marcarEnsaioRealizado(
        id: Long
    ) {

        val cursor =
            banco.readableDatabase.rawQuery(
                "SELECT data FROM agendamentos WHERE id = ?",
                arrayOf(
                    id.toString()
                )
            )

        var dataEnsaio = ""

        if (cursor.moveToFirst()) {
            dataEnsaio =
                cursor.getString(0) ?: ""
        }

        cursor.close()

        val prazo =
            calcularPrazo(dataEnsaio)

        val dados =
            ContentValues()

        dados.put(
            "realizado",
            1
        )

        dados.put(
            "prazo_entrega",
            prazo
        )

        dados.put(
            "entrega_realizada",
            0
        )

        banco.writableDatabase.update(
            "agendamentos",
            dados,
            "id = ?",
            arrayOf(
                id.toString()
            )
        )

        Toast.makeText(
            this,
            "Ensaio realizado. Prazo: $prazo",
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

            calendario.time =
                dataOriginal!!

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

    private fun buscarTodos(): MutableList<Agendamento> {

        val lista =
            mutableListOf<Agendamento>()

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
                """.trimIndent(),
                null
            )

        if (cursor.moveToFirst()) {

            do {

                lista.add(
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
                )

            } while (
                cursor.moveToNext()
            )
        }

        cursor.close()

        return lista
    }

    private fun listarAgendamentos() {

        telaAtual = "agendamentos"

        val tela =
            fundo(Color.WHITE)

        val principal =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_HORIZONTAL

                setPadding(
                    25,
                    20,
                    25,
                    20
                )
            }

        principal.addView(
            tituloInterno(
                "AGENDAMENTOS"
            )
        )

        principal.addView(
            botao(
                "NOVO AGENDAMENTO",
                true
            ) {
                novoAgendamento()
            }
        )

        val lista =
            buscarTodos()
                .filter {
                    !it.realizado
                }
                .sortedWith(
                    compareBy<Agendamento> {
                        dataOrdenacao(it.data)
                    }.thenBy {
                        it.horario
                    }
                )

        if (lista.isEmpty()) {

            principal.addView(
                TextView(this).apply {

                    text =
                        "Nenhum agendamento pendente."

                    textSize = 17f

                    gravity =
                        Gravity.CENTER

                    setTextColor(
                        cinzaTexto
                    )

                    setPadding(
                        0,
                        25,
                        0,
                        25
                    )
                }
            )

        } else {

            val grade =
                GridLayout(this).apply {

                    columnCount = 2

                    rowCount =
                        (lista.size + 1) / 2

                    useDefaultMargins = false

                    layoutParams =
                        LinearLayout.LayoutParams(
                            -1,
                            -2
                        )
                }

            lista.forEach { agendamento ->

                grade.addView(
                    cartaoAgendamento(
                        agendamento
                    )
                )
            }

            principal.addView(
                grade
            )
        }

        principal.addView(
            botao(
                "VOLTAR",
                true
            ) {
                telaInicial()
            }
        )

        tela.addView(
            principal,
            FrameLayout.LayoutParams(
                -1,
                -2,
                Gravity.CENTER
            )
        )

        setContentView(tela)
    }

    private fun cartaoAgendamento(
        agendamento: Agendamento
    ): LinearLayout {

        val card =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    12,
                    15,
                    12,
                    15
                )

                background =
                    GradientDrawable().apply {

                        cornerRadius = 18f

                        setColor(
                            Color.rgb(
                                250,
                                250,
                                250
                            )
                        )

                        setStroke(
                            2,
                            Color.rgb(
                                205,
                                190,
                                215
                            )
                        )
                    }

                isClickable = true
                isFocusable = true
            }

        val largura =
            resources.displayMetrics.widthPixels

        val tamanho =
            (largura - 80) / 2

        card.layoutParams =
            GridLayout.LayoutParams().apply {

                width =
                    tamanho

                height =
                    GridLayout.LayoutParams.WRAP_CONTENT

                setMargins(
                    6,
                    6,
                    6,
                    6
                )
            }

        card.addView(
            TextView(this).apply {

                text =
                    agendamento.nome.ifBlank {
                        "Cliente sem nome"
                    }

                textSize = 15f

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.BLACK
                )

                gravity =
                    Gravity.CENTER
            }
        )

        card.addView(
            TextView(this).apply {

                text =
                    if (agendamento.data.isBlank())
                        "Data não informada"
                    else
                        agendamento.data

                textSize = 13f

                setTextColor(
                    cinzaTexto
                )

                gravity =
                    Gravity.CENTER
            }
        )

        card.addView(
            TextView(this).apply {

                text =
                    if (agendamento.horario.isBlank())
                        "Horário não informado"
                    else
                        agendamento.horario

                textSize = 13f

                setTextColor(
                    cinzaTexto
                )

                gravity =
                    Gravity.CENTER
            }
        )

        card.setOnClickListener {
            detalhesAgendamento(agendamento)
        }

        return card
    }

    private fun detalhesAgendamento(
        agendamento: Agendamento
    ) {

        telaAtual =
            "detalhes_agendamento"

        val tela =
            fundo(Color.WHITE)

        val conteudo =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    30,
                    20,
                    30,
                    20
                )
            }

        conteudo.addView(
            tituloInterno(
                agendamento.nome.ifBlank {
                    "Detalhes do agendamento"
                }
            )
        )

        val restante =
            agendamento.valor -
                agendamento.sinal

        conteudo.addView(
            TextView(this).apply {

                text =
                    """
                    Contato:
                    ${agendamento.contato.ifBlank {
                        "Não informado"
                    }}

                    Data:
                    ${agendamento.data.ifBlank {
                        "Não informada"
                    }}

                    Horário:
                    ${agendamento.horario.ifBlank {
                        "Não informado"
                    }}

                    Tipo:
                    ${agendamento.tipo.ifBlank {
                        "Não informado"
                    }}

                    Valor:
                    R$ ${formatar(agendamento.valor)}

                    Sinal:
                    R$ ${formatar(agendamento.sinal)}

                    Restante:
                    R$ ${formatar(restante)}

                    Observações:
                    ${agendamento.observacoes.ifBlank {
                        "Nenhuma"
                    }}
                    """.trimIndent()

                textSize = 15f

                setTextColor(
                    Color.DKGRAY
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    10,
                    0,
                    20
                )
            }
        )

        conteudo.addView(
            botao(
                "EDITAR / REAGENDAR",
                true
            ) {
                novoAgendamento(agendamento)
            }
        )

        conteudo.addView(
            botao(
                "ENSAIO REALIZADO",
                true
            ) {
                marcarEnsaioRealizado(
                    agendamento.id
                )
            }
        )

        conteudo.addView(
            botao(
                "VOLTAR",
                true
            ) {
                listarAgendamentos()
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
    }

    private fun telaEntregas() {

        telaAtual =
            "entregas"

        val tela =
            fundo(Color.WHITE)

        val principal =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_HORIZONTAL

                setPadding(
                    25,
                    20,
                    25,
                    20
                )
            }

        principal.addView(
            tituloInterno(
                "ENTREGAS"
            )
        )

        val lista =
            buscarTodos()
                .filter {
                    it.realizado &&
                        !it.entregaRealizada
                }
                .sortedBy {
                    dataOrdenacao(
                        it.prazoEntrega ?: ""
                    )
                }

        if (lista.isEmpty()) {

            principal.addView(
                TextView(this).apply {

                    text =
                        "Nenhuma entrega pendente."

                    textSize = 17f

                    gravity =
                        Gravity.CENTER

                    setTextColor(
                        cinzaTexto
                    )

                    setPadding(
                        0,
                        25,
                        0,
                        25
                    )
                }
            )

        } else {

            val grade =
                GridLayout(this).apply {

                    columnCount = 2

                    rowCount =
                        (lista.size + 1) / 2

                    useDefaultMargins = false

                    layoutParams =
                        LinearLayout.LayoutParams(
                            -1,
                            -2
                        )
                }

            lista.forEach {

                grade.addView(
                    cartaoEntrega(it)
                )
            }

            principal.addView(
                grade
            )
        }

        principal.addView(
            botao(
                "VOLTAR",
                true
            ) {
                telaInicial()
            }
        )

        tela.addView(
            principal,
            FrameLayout.LayoutParams(
                -1,
                -2,
                Gravity.CENTER
            )
        )

        setContentView(tela)
    }

    private fun cartaoEntrega(
        agendamento: Agendamento
    ): LinearLayout {

        val card =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    10,
                    15,
                    10,
                    15
                )

                background =
                    GradientDrawable().apply {

                        cornerRadius = 18f

                        setColor(
                            Color.rgb(
                                250,
                                250,
                                250
                            )
                        )

                        setStroke(
                            2,
                            Color.rgb(
                                205,
                                190,
                                215
                            )
                        )
                    }

                isClickable = true
                isFocusable = true
            }

        val largura =
            resources.displayMetrics.widthPixels

        val tamanho =
            (largura - 80) / 2

        card.layoutParams =
            GridLayout.LayoutParams().apply {

                width =
                    tamanho

                height =
                    GridLayout.LayoutParams.WRAP_CONTENT

                setMargins(
                    6,
                    6,
                    6,
                    6
                )
            }

        card.addView(
            TextView(this).apply {

                text =
                    agendamento.nome.ifBlank {
                        "Cliente sem nome"
                    }

                textSize = 15f

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.BLACK
                )

                gravity =
                    Gravity.CENTER
            }
        )

        val prazo =
            agendamento.prazoEntrega
                ?: "Não definido"

        card.addView(
            TextView(this).apply {

                text =
                    "Prazo: $prazo"

                textSize = 13f

                setTextColor(
                    cinzaTexto
                )

                gravity =
                    Gravity.CENTER
            }
        )

        card.addView(
            TextView(this).apply {

                text =
                    calcularContagem(prazo)

                textSize = 14f

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.rgb(
                        95,
                        60,
                        110
                    )
                )

                gravity =
                    Gravity.CENTER
            }
        )

        card.setOnClickListener {
            detalhesEntrega(agendamento)
        }

        return card
    }

    private fun detalhesEntrega(
        agendamento: Agendamento
    ) {

        telaAtual =
            "detalhes_entrega"

        val tela =
            fundo(Color.WHITE)

        val conteudo =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    30,
                    20,
                    30,
                    20
                )
            }

        conteudo.addView(
            tituloInterno(
                agendamento.nome.ifBlank {
                    "Entrega"
                }
            )
        )

        val prazo =
            agendamento.prazoEntrega
                ?: "Não definido"

        conteudo.addView(
            TextView(this).apply {

                text =
                    """
                    Cliente:
                    ${agendamento.nome.ifBlank {
                        "Não informado"
                    }}

                    Data do ensaio:
                    ${agendamento.data.ifBlank {
                        "Não informada"
                    }}

                    Prazo máximo:
                    $prazo

                    ${calcularContagem(prazo)}
                    """.trimIndent()

                textSize = 16f

                setTextColor(
                    Color.DKGRAY
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    10,
                    0,
                    20
                )
            }
        )

        conteudo.addView(
            botao(
                "ALTERAR PRAZO",
                true
            ) {
                alterarPrazo(
                    agendamento.id
                )
            }
        )

        conteudo.addView(
            botao(
                "ENTREGA REALIZADA",
                true
            ) {
                marcarEntregaRealizada(
                    agendamento.id
                )
            }
        )

        conteudo.addView(
            botao(
                "VOLTAR",
                true
            ) {
                telaEntregas()
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
    }

    private fun calcularContagem(
        prazo: String
    ): String {

        if (prazo.isBlank())
            return "Prazo não definido"

        return try {

            val formato =
                SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale("pt", "BR")
                )

            formato.isLenient = false

            val dataPrazo =
                formato.parse(prazo)

            val hoje =
                Calendar.getInstance()

            hoje.set(
                Calendar.HOUR_OF_DAY,
                0
            )

            hoje.set(
                Calendar.MINUTE,
                0
            )

            hoje.set(
                Calendar.SECOND,
                0
            )

            hoje.set(
                Calendar.MILLISECOND,
                0
            )

            val prazoCalendario =
                Calendar.getInstance()

            prazoCalendario.time =
                dataPrazo!!

            prazoCalendario.set(
                Calendar.HOUR_OF_DAY,
                0
            )

            prazoCalendario.set(
                Calendar.MINUTE,
                0
            )

            prazoCalendario.set(
                Calendar.SECOND,
                0
            )

            prazoCalendario.set(
                Calendar.MILLISECOND,
                0
            )

            val diferenca =
                prazoCalendario.timeInMillis -
                    hoje.timeInMillis

            val dias =
                diferenca /
                    (24L * 60L * 60L * 1000L)

            when {

                dias > 1 ->
                    "Faltam $dias dias"

                dias == 1L ->
                    "Falta 1 dia"

                dias == 0L ->
                    "VENCE HOJE"

                dias == -1L ->
                    "Vencido há 1 dia"

                else ->
                    "Vencido há ${-dias} dias"
            }

        } catch (_: Exception) {

            "Prazo inválido"
        }
    }

    private fun alterarPrazo(
        id: Long
    ) {

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
                    arrayOf(
                        id.toString()
                    )
                )

                telaEntregas()

            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun marcarEntregaRealizada(
        id: Long
    ) {

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
            arrayOf(
                id.toString()
            )
        )

        Toast.makeText(
            this,
            "Entrega marcada como realizada.",
            Toast.LENGTH_SHORT
        ).show()

        telaEntregas()
    }

    private fun calendarioMensal(
        entregas: Boolean
    ) {

        telaAtual =
            if (entregas)
                "calendario_entregas"
            else
                "calendario_ensaios"

        val calendario =
            Calendar.getInstance()

        mostrarMes(
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            entregas
        )
    }

    private fun mostrarMes(
        ano: Int,
        mes: Int,
        entregas: Boolean
    ) {

        val tela =
            fundo(Color.WHITE)

        val principal =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_HORIZONTAL

                setPadding(
                    25,
                    20,
                    25,
                    20
                )
            }

        principal.addView(
            tituloInterno(
                if (entregas)
                    "ENTREGAS DO MÊS"
                else
                    "ENSAIOS DO MÊS"
            )
        )

        val navegacao =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER

                layoutParams =
                    LinearLayout.LayoutParams(
                        -1,
                        65
                    )
            }

        val anterior =
            TextView(this).apply {

                text = "‹"
                textSize = 35f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.BLACK
                )

                setOnClickListener {

                    val novoMes =
                        Calendar.getInstance()

                    novoMes.set(
                        ano,
                        mes,
                        1
                    )

                    novoMes.add(
                        Calendar.MONTH,
                        -1
                    )

                    mostrarMes(
                        novoMes.get(Calendar.YEAR),
                        novoMes.get(Calendar.MONTH),
                        entregas
                    )
                }

                layoutParams =
                    LinearLayout.LayoutParams(
                        90,
                        65
                    )
            }

        val nomeMes =
            TextView(this).apply {

                text =
                    nomeDoMes(
                        mes,
                        ano
                    )

                textSize = 20f

                typeface =
                    Typeface.DEFAULT_BOLD

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.rgb(
                        70,
                        55,
                        75
                    )
                )

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        65,
                        1f
                    )
            }

        val proximo =
            TextView(this).apply {

                text = "›"
                textSize = 35f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.BLACK
                )

                setOnClickListener {

                    val novoMes =
                        Calendar.getInstance()

                    novoMes.set(
                        ano,
                        mes,
                        1
                    )

                    novoMes.add(
                        Calendar.MONTH,
                        1
                    )

                    mostrarMes(
                        novoMes.get(Calendar.YEAR),
                        novoMes.get(Calendar.MONTH),
                        entregas
                    )
                }

                layoutParams =
                    LinearLayout.LayoutParams(
                        90,
                        65
                    )
            }

        navegacao.addView(anterior)
        navegacao.addView(nomeMes)
        navegacao.addView(proximo)

        principal.addView(navegacao)

        principal.addView(
            TextView(this).apply {

                text =
                    "SEG   TER   QUA   QUI   SEX   SÁB   DOM"

                textSize = 11f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    cinzaTexto
                )

                setPadding(
                    0,
                    10,
                    0,
                    10
                )
            }
        )

        val gradeCalendario =
            GridLayout(this).apply {

                columnCount = 7
                rowCount = 6
                useDefaultMargins = false

                layoutParams =
                    LinearLayout.LayoutParams(
                        -1,
                        -2
                    )
            }

        val primeiroDia =
            Calendar.getInstance()

        primeiroDia.set(
            ano,
            mes,
            1
        )

        val diaSemana =
            (
                primeiroDia.get(
                    Calendar.DAY_OF_WEEK
                ) + 5
                ) % 7

        val ultimoDia =
            primeiroDia.getActualMaximum(
                Calendar.DAY_OF_MONTH
            )

        for (i in 0 until 42) {

            val dia =
                i - diaSemana + 1

            if (
                dia < 1 ||
                dia > ultimoDia
            ) {

                gradeCalendario.addView(
                    TextView(this).apply {

                        layoutParams =
                            GridLayout.LayoutParams().apply {

                                width = 0
                                height = 55

                                columnSpec =
                                    GridLayout.spec(
                                        GridLayout.UNDEFINED,
                                        1f
                                    )
                            }
                    }
                )

            } else {

                val quantidade =
                    quantidadeNoDia(
                        dia,
                        mes,
                        ano,
                        entregas
                    )

                val botaoDia =
                    TextView(this).apply {

                        text =
                            if (quantidade > 0)
                                "$dia\n●"
                            else
                                dia.toString()

                        textSize = 13f

                        gravity =
                            Gravity.CENTER

                        setTextColor(
                            Color.BLACK
                        )

                        background =
                            GradientDrawable().apply {

                                cornerRadius = 12f

                                setColor(
                                    if (quantidade > 0)
                                        lilasClaro
                                    else
                                        Color.WHITE
                                )

                                setStroke(
                                    1,
                                    Color.rgb(
                                        220,
                                        220,
                                        220
                                    )
                                )
                            }

                        setOnClickListener {

                            mostrarDiaDoMes(
                                dia,
                                mes,
                                ano,
                                entregas
                            )
                        }

                        layoutParams =
                            GridLayout.LayoutParams().apply {

                                width = 0
                                height = 55

                                columnSpec =
                                    GridLayout.spec(
                                        GridLayout.UNDEFINED,
                                        1f
                                    )

                                setMargins(
                                    2,
                                    2,
                                    2,
                                    2
                                )
                            }
                    }

                gradeCalendario.addView(
                    botaoDia
                )
            }
        }

        principal.addView(
            gradeCalendario
        )

        principal.addView(
            botao(
                "VOLTAR",
                true
            ) {
                telaInicial()
            }
        )

        tela.addView(
            principal,
            FrameLayout.LayoutParams(
                -1,
                -2,
                Gravity.CENTER
            )
        )

        setContentView(tela)
    }

    private fun quantidadeNoDia(
        dia: Int,
        mes: Int,
        ano: Int,
        entregas: Boolean
    ): Int {

        return buscarTodos().count {

            val data =
                if (entregas)
                    it.prazoEntrega
                else
                    it.data

            if (data.isNullOrBlank()) {

                false

            } else {

                val partes =
                    data.split("/")

                if (partes.size != 3) {

                    false

                } else {

                    partes[0].toIntOrNull() == dia &&
                        partes[1].toIntOrNull() == mes + 1 &&
                        partes[2].toIntOrNull() == ano
                }
            }
        }
    }

    private fun mostrarDiaDoMes(
        dia: Int,
        mes: Int,
        ano: Int,
        entregas: Boolean
    ) {

        val dataSelecionada =
            String.format(
                Locale("pt", "BR"),
                "%02d/%02d/%04d",
                dia,
                mes + 1,
                ano
            )

        val lista =
            buscarTodos()
                .filter {

                    val data =
                        if (entregas)
                            it.prazoEntrega
                        else
                            it.data

                    data == dataSelecionada
                }
                .sortedWith(
                    compareBy<Agendamento> {
                        it.data
                    }.thenBy {
                        it.horario
                    }
                )

        val tela =
            fundo(Color.WHITE)

        val principal =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_HORIZONTAL

                setPadding(
                    25,
                    20,
                    25,
                    20
                )
            }

        principal.addView(
            tituloInterno(
                dataSelecionada
            )
        )

        if (lista.isEmpty()) {

            principal.addView(
                TextView(this).apply {

                    text =
                        if (entregas)
                            "Nenhuma entrega neste dia."
                        else
                            "Nenhum ensaio neste dia."

                    textSize = 16f

                    gravity =
                        Gravity.CENTER

                    setTextColor(
                        cinzaTexto
                    )

                    setPadding(
                        0,
                        25,
                        0,
                        25
                    )
                }
            )

        } else {

            val grade =
                GridLayout(this).apply {

                    columnCount = 2

                    rowCount =
                        (lista.size + 1) / 2

                    useDefaultMargins = false
                }

            lista.forEach {

                if (entregas) {

                    grade.addView(
                        cartaoEntrega(it)
                    )

                } else {

                    grade.addView(
                        cartaoAgendamento(it)
                    )
                }
            }

            principal.addView(
                grade
            )
        }

        principal.addView(
            botao(
                "VOLTAR",
                true
            ) {

                mostrarMes(
                    ano,
                    mes,
                    entregas
                )
            }
        )

        tela.addView(
            principal,
            FrameLayout.LayoutParams(
                -1,
                -2,
                Gravity.CENTER
            )
        )

        setContentView(tela)
    }

    private fun dataOrdenacao(
        data: String
    ): String {

        if (data.isBlank())
            return "9999-99-99"

        val partes =
            data.split("/")

        if (partes.size != 3)
            return "9999-99-99"

        return "${partes[2]}-${partes[1]}-${partes[0]}"
    }

    private fun nomeDoMes(
        mes: Int,
        ano: Int
    ): String {

        val nomes =
            arrayOf(
                "JANEIRO",
                "FEVEREIRO",
                "MARÇO",
                "ABRIL",
                "MAIO",
                "JUNHO",
                "JULHO",
                "AGOSTO",
                "SETEMBRO",
                "OUTUBRO",
                "NOVEMBRO",
                "DEZEMBRO"
            )

        return "${nomes[mes]} $ano"
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
