package com.patreze.estudioagendamentos

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
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
    val sinal: Double
)

class Banco(activity: Activity) : SQLiteOpenHelper(
    activity,
    "estudio.db",
    null,
    1
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
                sinal REAL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        antiga: Int,
        nova: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS agendamentos")
        onCreate(db)
    }
}

class MainActivity : Activity() {

    private lateinit var banco: Banco

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        banco = Banco(this)

        telaInicial()
    }

    private fun telaBase(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(35, 35, 35, 35)
        }
    }

    private fun titulo(texto: String): TextView {
        return TextView(this).apply {
            text = texto
            textSize = 26f
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 30)
            layoutParams = LinearLayout.LayoutParams(
                -1,
                -2
            )
        }
    }

    private fun campo(dica: String): EditText {
        return EditText(this).apply {
            hint = dica
            textSize = 16f
            setPadding(15, 10, 15, 10)

            layoutParams = LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                setMargins(0, 0, 0, 14)
            }
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

            layoutParams = LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }
    }

    private fun telaCentralizada(
        conteudo: LinearLayout,
        rolagem: Boolean = false
    ) {

        if (rolagem) {

            val scroll =
                ScrollView(this)

            scroll.addView(
                conteudo,
                ViewGroup.LayoutParams(
                    -1,
                    -2
                )
            )

            setContentView(scroll)

        } else {

            val frame =
                FrameLayout(this)

            frame.addView(
                conteudo,
                FrameLayout.LayoutParams(
                    -1,
                    -2,
                    Gravity.CENTER
                )
            )

            setContentView(frame)
        }
    }

    private fun telaInicial() {

        val layout = telaBase()

        layout.gravity =
            Gravity.CENTER

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
                telaEntregas()
            }
        )

        telaCentralizada(layout)
    }

    private fun novoAgendamento(
        editar: Agendamento? = null
    ) {

        val layout = telaBase()

        layout.addView(
            titulo(
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

        observacoes.minLines = 4

        val valor =
            campo(
                "Valor total do ensaio — somente o número"
            )

        valor.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER

        val sinal =
            campo(
                "Valor do SINAL — somente o número"
            )

        sinal.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER

        if (editar != null) {

            nome.setText(editar.nome)
            contato.setText(editar.contato)
            data.setText(editar.data)
            horario.setText(editar.horario)
            tipo.setText(editar.tipo)
            observacoes.setText(
                editar.observacoes
            )

            if (editar.valor > 0) {
                valor.setText(
                    editar.valor
                        .toInt()
                        .toString()
                )
            }

            if (editar.sinal > 0) {
                sinal.setText(
                    editar.sinal
                        .toInt()
                        .toString()
                )
            }
        }

        layout.addView(nome)
        layout.addView(contato)
        layout.addView(data)
        layout.addView(horario)
        layout.addView(tipo)
        layout.addView(observacoes)
        layout.addView(valor)
        layout.addView(sinal)

        layout.addView(
            botao(
                if (editar == null)
                    "SALVAR AGENDAMENTO"
                else
                    "SALVAR ALTERAÇÕES"
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

        layout.addView(
            botao("VOLTAR") {
                telaInicial()
            }
        )

        telaCentralizada(
            layout,
            rolagem = true
        )
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
            if (valorTexto.isBlank())
                0.0
            else
                valorTexto.toDoubleOrNull()
                    ?: 0.0

        val sinal =
            if (sinalTexto.isBlank())
                0.0
            else
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
                "Agendamento cadastrado com sucesso.",
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

                    if (alterando) return

                    val numeros =
                        s.toString()
                            .filter {
                                it.isDigit()
                            }
                            .take(11)

                    if (numeros.isEmpty()) return

                    val formatado =
                        when {

                            numeros.length <= 2 ->
                                "(${numeros}"

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

    private fun listarAgendamentos() {

        val layout =
            telaBase()

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
                    id,
                    nome,
                    contato,
                    data,
                    horario,
                    tipo,
                    observacoes,
                    valor,
                    sinal
                FROM agendamentos
                ORDER BY data, horario
                """.trimIndent(),
                null
            )

        if (!cursor.moveToFirst()) {

            val aviso =
                TextView(this)

            aviso.text =
                "Nenhum agendamento cadastrado."

            aviso.textSize = 17f

            aviso.gravity =
                Gravity.CENTER

            aviso.setPadding(
                0,
                30,
                0,
                30
            )

            layout.addView(aviso)

        } else {

            do {

                val agendamento =
                    Agendamento(
                        cursor.getLong(0),
                        cursor.getString(1)
                            ?: "",
                        cursor.getString(2)
                            ?: "",
                        cursor.getString(3)
                            ?: "",
                        cursor.getString(4)
                            ?: "",
                        cursor.getString(5)
                            ?: "",
                        cursor.getString(6)
                            ?: "",
                        cursor.getDouble(7),
                        cursor.getDouble(8)
                    )

                layout.addView(
                    cardAgendamento(
                        agendamento
                    )
                )

            } while (cursor.moveToNext())
        }

        cursor.close()

        layout.addView(
            botao("VOLTAR") {
                telaInicial()
            }
        )

        telaCentralizada(
            layout,
            rolagem = true
        )
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
            20,
            0,
            20
        )

        val restante =
            agendamento.valor -
                agendamento.sinal

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

            Valor: R$ ${formatar(
                agendamento.valor
            )}

            Sinal: R$ ${formatar(
                agendamento.sinal
            )}

            Restante: R$ ${formatar(
                restante
            )}

            Observações:
            ${
                agendamento.observacoes.ifBlank {
                    "Nenhuma"
                }
            }
            """.trimIndent()

        texto.textSize = 16f

        texto.gravity =
            Gravity.CENTER

        card.addView(texto)

        card.addView(
            botao("EDITAR / REAGENDAR") {
                novoAgendamento(
                    agendamento
                )
            }
        )

        return card
    }

    private fun telaEntregas() {

        val layout =
            telaBase()

        layout.addView(
            titulo("ENTREGAS")
        )

        val aviso =
            TextView(this)

        aviso.text =
            "A parte de entregas será adicionada nesta etapa."

        aviso.textSize = 17f

        aviso.gravity =
            Gravity.CENTER

        aviso.setPadding(
            0,
            30,
            0,
            30
        )

        layout.addView(aviso)

        layout.addView(
            botao("VOLTAR") {
                telaInicial()
            }
        )

        telaCentralizada(layout)
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
