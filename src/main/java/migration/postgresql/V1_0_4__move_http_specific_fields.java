package migration.postgresql;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.ResultSet;
import java.sql.Statement;

public class V1_0_4__move_http_specific_fields extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (Statement updateAndGet = context.getConnection().createStatement()) {
            Statement updateOnly = context.getConnection().createStatement();
            updateAndGet.execute("CREATE TABLE monitor_http_info (id bigserial not null, allowed_http_codes text not null, verify_certificate boolean not null, monitor_id bigint, primary key (id));");
            updateAndGet.execute("ALTER TABLE monitors ADD COLUMN monitor_http_info_id bigint;");
            updateAndGet.execute("ALTER TABLE monitors ADD CONSTRAINT monitors_http_inf_id_fk FOREIGN KEY (monitor_http_info_id) REFERENCES monitor_http_info;");
            updateAndGet.execute("ALTER TABLE monitor_http_info ADD CONSTRAINT monitor_http_inf_monitor_id_fk FOREIGN KEY (monitor_id) REFERENCES monitors;");
            try (ResultSet resultSet = updateAndGet.executeQuery("INSERT INTO monitor_http_info (monitor_id, allowed_http_codes, verify_certificate) (SELECT id AS monitor_id, allowed_http_codes, verify_certificate FROM monitors WHERE type = 0) RETURNING id, monitor_id;")) {
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

