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

class Banco(
    activity: Activity
) : SQLiteOpenHelper(
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
                nome TEXT NOT NULL,
                contato TEXT NOT NULL,
                data TEXT NOT NULL,
                horario TEXT NOT NULL,
                tipo TEXT NOT NULL,
                observacoes TEXT,
                valor REAL NOT NULL,
                sinal REAL NOT NULL
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

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        banco = Banco(this)

        telaInicial()
    }

    private fun base(): LinearLayout {

        return LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            setPadding(
                30,
                30,
                30,
                30
            )
        }
    }

    private fun titulo(
        texto: String
    ): TextView {

        return TextView(this).apply {

            text = texto

            textSize = 26f

            gravity =
                Gravity.CENTER

            setPadding(
                0,
                0,
                0,
                30
            )
        }
    }

    private fun campo(
        dica: String
    ): EditText {

        return EditText(this).apply {

            hint = dica

            textSize = 16f

            setPadding(
                15,
                15,
                15,
                15
            )

            layoutParams =
                LinearLayout.LayoutParams(
                    -1,
                    -2
                ).apply {
                    setMargins(
                        0,
                        0,
                        0,
                        12
                    )
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

            layoutParams =
                LinearLayout.LayoutParams(
                    -1,
                    -2
                ).apply {
                    setMargins(
                        0,
                        8,
                        0,
                        8
                    )
                }
        }
    }

    private fun telaInicial() {

        val tela =
            FrameLayout(this)

        val layout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    40,
                    40,
                    40,
                    40
                )
            }

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

        tela.addView(
            layout,
            FrameLayout.LayoutParams(
                -1,
                -2,
                Gravity.CENTER
            )
        )

        setContentView(tela)
    }

    private fun novoAgendamento(
        editar: Agendamento? = null
    ) {

        val layout = base()

        layout.addView(
            titulo(
                if (editar == null)
                    "NOVO AGENDAMENTO"
                else
                    "EDITAR AGENDAMENTO"
            )
        )

        val nome =
            campo("Nome do cliente")

        val contato =
            campo("Contato / WhatsApp")

        val data =
            campo("Data do ensaio — DD/MM/AAAA")

        val horario =
            campo("Horário do ensaio — HH:MM")

        val tipo =
            campo("Tipo de ensaio")

        val observacoes =
            campo("Observações")

        observacoes.minLines = 4

        val valor =
            campo("Valor total — exemplo: 300,00")

        val sinal =
            campo("Valor do sinal — deixe 0 se não houver")

        data.isFocusable = false
        horario.isFocusable = false

        data.setOnClickListener {
            escolherData(data)
        }

        horario.setOnClickListener {
            escolherHorario(horario)
        }

        if (editar != null) {

            nome.setText(editar.nome)
            contato.setText(editar.contato)
            data.setText(editar.data)
            horario.setText(editar.horario)
            tipo.setText(editar.tipo)
            observacoes.setText(
                editar.observacoes
            )

            valor.setText(
                editar.valor.toString()
            )

            sinal.setText(
                editar.sinal.toString()
            )

        } else {

            sinal.setText("0")
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

        setContentView(
            ScrollView(this).apply {
                addView(layout)
            }
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

        if (
            nome.isBlank() ||
            contato.isBlank() ||
            data.isBlank() ||
            horario.isBlank() ||
            tipo.isBlank() ||
            valorTexto.isBlank()
        ) {

            Toast.makeText(
                this,
                "Preencha os campos obrigatórios.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val valor =
            dinheiro(valorTexto)

        val sinal =
            if (sinalTexto.isBlank())
                0.0
            else
                dinheiro(sinalTexto)

        if (valor == null) {

            Toast.makeText(
                this,
                "Digite um valor válido.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (sinal == null) {

            Toast.makeText(
                this,
                "Digite um sinal válido.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (sinal > valor) {

            Toast.makeText(
                this,
                "O sinal não pode ser maior que o valor.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

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

        telaInicial()
    }

    private fun dinheiro(
        texto: String
    ): Double? {

        return try {

            texto
                .replace("R$", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", ".")
                .toDouble()

        } catch (_: Exception) {

            null
        }
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

        val layout = base()

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
                        id =
                            cursor.getLong(0),
                        nome =
                            cursor.getString(1),
                        contato =
                            cursor.getString(2),
                        data =
                            cursor.getString(3),
                        horario =
                            cursor.getString(4),
                        tipo =
                            cursor.getString(5),
                        observacoes =
                            cursor.getString(6)
                                ?: "",
                        valor =
                            cursor.getDouble(7),
                        sinal =
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

        setContentView(
            ScrollView(this).apply {
                addView(layout)
            }
        )
    }

    private fun cardAgendamento(
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
            agendamento.valor -
                agendamento.sinal

        val texto =
            TextView(this)

        texto.text =
            """
            ${agendamento.nome}

            Contato: ${agendamento.contato}

            Data: ${agendamento.data}
            Horário: ${agendamento.horario}

            Tipo: ${agendamento.tipo}

            Valor: R$ ${formatar(agendamento.valor)}
            Sinal: R$ ${formatar(agendamento.sinal)}
            Restante: R$ ${formatar(restante)}

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

        return card
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

    private fun telaEntregas() {

        val layout = base()

        layout.addView(
            titulo("ENTREGAS")
        )

        val aviso =
            TextView(this)

        aviso.text =
            "A parte de entregas será adicionada depois que o cadastro de agendamentos estiver funcionando corretamente."

        aviso.textSize = 17f

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

        setContentView(layout)
    }
}
