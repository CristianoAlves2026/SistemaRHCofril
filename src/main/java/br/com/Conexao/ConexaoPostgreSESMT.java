package br.com.Conexao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConexaoPostgreSESMT {

    private static HikariDataSource dataSource;

    static {
        try {

            HikariConfig config = new HikariConfig();

            // 🔥 BANCO CORRETO (ALTERADO AQUI)
            config.setJdbcUrl(
                "jdbc:postgresql://ep-aged-mud-acujtw7g-pooler.sa-east-1.aws.neon.tech:5432/bdsesmt" +
                "?sslmode=require&channelBinding=require"
            );

            config.setUsername("neondb_owner");
            config.setPassword("npg_c2NT9unRCFpd");

            // 🔥 CONFIGURAÇÃO DO POOL
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(0);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(30000);
            config.setMaxLifetime(600000);
            config.setValidationTimeout(10000);

            // 🔥 IMPORTANTE PRO NEON
            config.setInitializationFailTimeout(-1);

            dataSource = new HikariDataSource(config);

            System.out.println("✅ Pool PostgreSQL iniciado (bdsesmt)");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao inicializar conexão: " + e.getMessage());
        }
    }

    public static Connection conectar() throws SQLException {
        return dataSource.getConnection();
    }
    
    public static void fecharPool() {
    try {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("🛑 Pool de conexões (HikariCP) encerrado com sucesso. Neon livre para desligar.");
        }
    } catch (Exception e) {
        System.err.println("Erro ao fechar o pool de conexões: " + e.getMessage());
    }
}
    
}