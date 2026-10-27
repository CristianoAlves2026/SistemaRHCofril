package br.com.Utilidades;

import br.com.Conexao.ConexaoPostgres;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NotificacaoServices {

    private static String notificacoes = "🔔 NOTIFICAÇÕES\n\nAtualizando informações...";
    private static boolean carregando = true;
    private static boolean iniciado = false;
    private static final java.util.List<FuncionarioNotificacao> funcionarios
            = new java.util.ArrayList<>();
    private static javax.swing.Timer timerAtualizacao;

    public static void iniciarAtualizacao() {

    if (iniciado) {
        return;
    }

    iniciado = true;

    // Atualiza imediatamente ao abrir o sistema
    new Thread(() -> atualizarNotificacoes()).start();

    // Agenda a próxima atualização
    agendarProximaAtualizacao();

}
    
    private static void agendarProximaAtualizacao() {

    java.time.LocalDateTime agora = java.time.LocalDateTime.now();
    java.time.LocalDateTime proximaAtualizacao;

    if (agora.getHour() < 6) {

        // Antes das 06:00 → agenda para hoje às 06:00
        proximaAtualizacao = agora.withHour(6)
                                  .withMinute(0)
                                  .withSecond(0)
                                  .withNano(0);

    } else if (agora.getHour() >= 18) {

        // Após as 18:00 → agenda para amanhã às 06:00
        proximaAtualizacao = agora.plusDays(1)
                                  .withHour(6)
                                  .withMinute(0)
                                  .withSecond(0)
                                  .withNano(0);

    } else {

        // Entre 06:00 e 17:59 → agenda para a próxima hora cheia
        proximaAtualizacao = agora.plusHours(1)
                                  .withMinute(0)
                                  .withSecond(0)
                                  .withNano(0);

    }

    long tempoEspera = java.time.Duration.between(agora, proximaAtualizacao).toMillis();

    if (timerAtualizacao != null) {
        timerAtualizacao.stop();
    }

    timerAtualizacao = new javax.swing.Timer((int) tempoEspera, e -> {

        timerAtualizacao.stop();

        new Thread(() -> atualizarEReagendar()).start();

    });

    timerAtualizacao.setRepeats(false);
    timerAtualizacao.start();

}
    
    private static void atualizarEReagendar() {

    try {

        atualizarNotificacoes();

    } finally {

        agendarProximaAtualizacao();

    }

}

    public static String getNotificacoes() {

        return notificacoes;

    }

    public static void atualizarNotificacoes() {

        carregando = true;

        notificacoes
                = "🔔 NOTIFICAÇÕES\n\nAtualizando informações...";

        try ( Connection con = ConexaoPostgres.conectar()) {

            carregarFuncionarios(con);

        } catch (Exception e) {

            e.printStackTrace();

        }

        StringBuilder texto = new StringBuilder();

        texto.append("🔔 NOTIFICAÇÕES\n\n");

        texto.append(buscarAniversariantes());

        texto.append("\n\n");

        texto.append(buscarAdmissoes());

        texto.append("\n\n");

        texto.append(buscarDemissoes());

        texto.append("\n\n");

        texto.append(buscarFuncionariosProcesso());

        texto.append("\n\n");

        texto.append(buscarExperiencia());

        // grava resultado final
        notificacoes = texto.toString();

        carregando = false;

    }

    private static String buscarAniversariantes() {

        String retorno = "🎂 Nenhum aniversariante hoje.";

        StringBuilder nomes = new StringBuilder();

        int total = 0;

        java.time.LocalDate hoje = java.time.LocalDate.now();

        for (FuncionarioNotificacao f : funcionarios) {

            if (!"ATIVO".equals(f.getStatus())) {
                continue;
            }

            if (f.getNascimento() == null) {
                continue;
            }

            if (f.getNascimento().getDayOfMonth() == hoje.getDayOfMonth()
                    && f.getNascimento().getMonthValue() == hoje.getMonthValue()) {

                if (total < 2) {

                    if (nomes.length() > 0) {
                        nomes.append(", ");
                    }

                    nomes.append(f.getNome());

                }

                total++;

            }

        }

        if (total == 1) {

            retorno = "🎂 " + nomes + " faz aniversário hoje.";

        } else if (total == 2) {

            retorno = "🎂 " + nomes + " fazem aniversário hoje.";

        } else if (total > 2) {

            retorno = "🎂 " + nomes + " e mais "
                    + (total - 2)
                    + " pessoas fazem aniversário hoje.";

        }

        return retorno;

    }

    private static String buscarAdmissoes() {

        int total = 0;

        LocalDate hoje = LocalDate.now();

        for (FuncionarioNotificacao f : funcionarios) {

            if (f.getAdmissao() == null) {
                continue;
            }

            if (f.getAdmissao().getYear() == hoje.getYear()
                    && f.getAdmissao().getMonthValue() == hoje.getMonthValue()) {

                total++;

            }

        }

        if (total == 0) {
            return "📥 Nenhuma admissão neste mês.";
        }

        if (total == 1) {
            return "📥 1 funcionário admitido neste mês.";
        }

        return "📥 " + total + " funcionários admitidos neste mês.";

    }

    private static String buscarDemissoes() {

        int total = 0;

        LocalDate hoje = LocalDate.now();

        for (FuncionarioNotificacao f : funcionarios) {

            if (f.getDemissao() == null) {
                continue;
            }

            if (f.getDemissao().getYear() == hoje.getYear()
                    && f.getDemissao().getMonthValue() == hoje.getMonthValue()) {

                total++;

            }

        }

        if (total == 0) {
            return "📤 Nenhuma demissão neste mês.";
        }

        if (total == 1) {
            return "📤 1 funcionário desligado neste mês.";
        }

        return "📤 " + total + " funcionários desligados neste mês.";

    }

    private static String buscarFuncionariosProcesso() {

        int total = 0;

        for (FuncionarioNotificacao f : funcionarios) {

            if ("EM PROCESSO".equals(f.getStatus())) {
                total++;
            }

        }

        if (total == 0) {
            return "⚠ Nenhum funcionário em processo de admissão.";
        }

        if (total == 1) {
            return "⚠ 1 funcionário em processo de admissão.";
        }

        return "⚠ " + total + " funcionários em processo de admissão.";

    }

    private static String buscarExperiencia() {

    int total = 0;
    int vencendoHoje = 0;
    int vencendoProximos5Dias = 0;

    LocalDate hoje = LocalDate.now();

    for (FuncionarioNotificacao f : funcionarios) {

        if (!"ATIVO".equals(f.getStatus())) {
            continue;
        }

        if (f.getAdmissao() == null) {
            continue;
        }

        if (!f.getAdmissao().isBefore(hoje.minusDays(90))) {

            total++;

            LocalDate vencimento = f.getAdmissao().plusDays(90);

            if (vencimento.equals(hoje)) {

                vencendoHoje++;

            } else if (vencimento.isAfter(hoje)
                    && !vencimento.isAfter(hoje.plusDays(5))) {

                vencendoProximos5Dias++;

            }

        }

    }

    StringBuilder texto = new StringBuilder();

    switch (total) {

        case 0 ->
            texto.append("🕒 Nenhum funcionário em período de experiência.");

        case 1 ->
            texto.append("🕒 1 funcionário em período de experiência.");

        default ->
            texto.append("🕒 ")
                 .append(total)
                 .append(" funcionários em período de experiência.");

    }

    switch (vencendoHoje) {

        case 0 ->
            texto.append(" Nenhum vencendo hoje.");

        case 1 ->
            texto.append(" 1 vencendo hoje.");

        default ->
            texto.append(" ")
                 .append(vencendoHoje)
                 .append(" vencendo hoje.");

    }

    switch (vencendoProximos5Dias) {

        case 0 ->
            texto.append(" Nenhum nos próximos cinco dias.");

        case 1 ->
            texto.append(" 1 vencendo nos próximos cinco dias.");

        default ->
            texto.append(" ")
                 .append(vencendoProximos5Dias)
                 .append(" vencendo nos próximos cinco dias.");

    }

    return texto.toString();
}

    private static void carregarFuncionarios(Connection con) {

        funcionarios.clear();

        String sql = """
        SELECT
            "EMPRESA",
            "NOME",
            "SETOR",
            "ADMISSAO",
            "DEMISSAO",
            "NASCIMENTO",
            "STATUS"
        FROM "Funcionarios"
        """;

        try ( PreparedStatement ps = con.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                try {
                    FuncionarioNotificacao f = new FuncionarioNotificacao();

                    f.setEmpresa(rs.getString("EMPRESA"));
                    f.setNome(rs.getString("NOME"));
                    f.setSetor(rs.getString("SETOR"));

                    f.setAdmissao(rs.getDate("ADMISSAO") == null
                            ? null
                            : rs.getDate("ADMISSAO").toLocalDate());

                    f.setDemissao(rs.getDate("DEMISSAO") == null
                            ? null
                            : rs.getDate("DEMISSAO").toLocalDate());

                    String nascimento = rs.getString("NASCIMENTO");

                    if (nascimento != null && nascimento.matches("\\d{2}/\\d{2}/\\d{4}")) {

                        try {

                            DateTimeFormatter formatter
                                    = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                            f.setNascimento(LocalDate.parse(nascimento, formatter));

                        } catch (Exception ex) {

                            f.setNascimento(null);


                        }

                    } else {

                        f.setNascimento(null);

                    }

                    f.setStatus(rs.getString("STATUS"));

                    funcionarios.add(f);

                } catch (Exception ex) {

                    System.out.println(
                            "Erro no funcionário: "
                            + rs.getString("NOME"));

                    ex.printStackTrace();

                }

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public static java.util.List<FuncionarioNotificacao> obterFuncionariosEmExperiencia() {

        java.util.List<FuncionarioNotificacao> lista = new java.util.ArrayList<>();

        java.time.LocalDate hoje = java.time.LocalDate.now();

        for (FuncionarioNotificacao f : funcionarios) {

            if (!"ATIVO".equals(f.getStatus())) {
                continue;
            }

            if (f.getAdmissao() == null) {
                continue;
            }

            if (!f.getAdmissao().isBefore(hoje.minusDays(90))) {
                lista.add(f);
            }
        }

        return lista;
    }

    public static java.util.List<FuncionarioNotificacao> obterAniversariantesDoDia() {
        java.util.List<FuncionarioNotificacao> aniversariantes = new java.util.ArrayList<>();
        LocalDate hoje = LocalDate.now();

        for (FuncionarioNotificacao f : funcionarios) {
            if (!"ATIVO".equals(f.getStatus())) {
                continue;
            }
            if (f.getNascimento() == null) {
                continue;
            }

            if (f.getNascimento().getDayOfMonth() == hoje.getDayOfMonth()
                    && f.getNascimento().getMonthValue() == hoje.getMonthValue()) {
                aniversariantes.add(f);
            }
        }
        return aniversariantes;
    }
    
    
    public static java.util.List<FuncionarioNotificacao> obterFuncionariosEmProcesso() {

    java.util.List<FuncionarioNotificacao> lista = new java.util.ArrayList<>();

    for (FuncionarioNotificacao f : funcionarios) {

        if ("EM PROCESSO".equals(f.getStatus())) {
            lista.add(f);
        }

    }

    return lista;

}

}
