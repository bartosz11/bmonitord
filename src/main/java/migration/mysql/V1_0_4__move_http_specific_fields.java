package migration.mysql;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.ResultSet;
import java.sql.Statement;

public class V1_0_4__move_http_specific_fields extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (Statement updateAndGet = context.getConnection().createStatement()) {
            Statement updateOnly = context.getConnection().createStatement();
            updateAndGet.execute("CREATE TABLE monitor_http_info (id bigint not null auto_increment, allowed_http_codes text not null, verify_certificate boolean not null, monitor_id bigint, primary key (id));");
            updateAndGet.execute("ALTER TABLE monitors ADD COLUMN monitor_http_info_id bigint;");
            updateAndGet.execute("ALTER TABLE monitors ADD CONSTRAINT monitors_http_inf_id_fk FOREIGN KEY (monitor_http_info_id) REFERENCES monitor_http_info (id);");
            updateAndGet.execute("ALTER TABLE monitor_http_info ADD CONSTRAINT monitor_http_inf_monitor_id_fk FOREIGN KEY (monitor_id) REFERENCES monitors (id);");
            updateAndGet.execute("INSERT INTO monitor_http_info (monitor_id, allowed_http_codes, verify_certificate) SELECT id AS monitor_id, allowed_http_codes, verify_certificate FROM monitors WHERE type = 0;");
            //most shitty way to do this, thanks MySQL for not having "RETURNING" like every single database that exists including fucking SQLite
            try (ResultSet resultSet = updateAndGet.executeQuery("SELECT id, monitor_id FROM monitor_http_info;")) {
                while (resultSet.next()) {
                    //there might be a more optimal way
                    long id = resultSet.getLong("id");
                    long monitorId = resultSet.getLong("monitor_id");
                    updateOnly.execute("UPDATE monitors SET monitor_http_info_id = " + id + " WHERE id =" + monitorId);
                }
            }
            updateAndGet.execute("ALTER TABLE monitors DROP COLUMN allowed_http_codes, DROP COLUMN verify_certificate;");
            updateOnly.close();
        }
    }

}

