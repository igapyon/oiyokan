package jp.oiyokan;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.data.OiyokanResourceSqlUtil;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;

public class OiyokanUnittestUtil {
    private static final Log log = LogFactory.getLog(OiyokanUnittestUtil.class);

    private static final String[][] OIYOKAN_FILE_SQLS = new String[][] { //
            { "oiyoUnitTestDb", "oiyokan-unittest-db-h2.sql" }, //
    };

    public static synchronized OiyoInfo setupUnittestDatabase() throws ODataApplicationException {
        final OiyoInfo oiyoInfo = OiyokanEdmProvider.getOiyoInfoInstance();
        boolean isUnittestDatabaseExists = false;
        for (OiyoSettingsDatabase database : oiyoInfo.getSettings().getDatabase()) {
            if (database.getName().equals("oiyoUnitTestDb")) {
                isUnittestDatabaseExists = true;
            }
        }
        if (isUnittestDatabaseExists) {
            // Already loaded.
            return oiyoInfo;
        }

        if (true) {
            String settings = "oiyokan/oiyokan-unittest-settings.json";
            log.info("OData v4: resources: load: " + settings);
            // resources から読み込み。
            final ClassPathResource cpres = new ClassPathResource(settings);
            try (InputStream inStream = cpres.getInputStream()) {
                final String strOiyokanSettings = StreamUtils.copyToString(inStream, Charset.forName("UTF-8"));

                final ObjectMapper mapper = new ObjectMapper();
                final OiyoSettings loadedSettings = mapper.readValue(strOiyokanSettings, OiyoSettings.class);
                for (OiyoSettingsDatabase database : loadedSettings.getDatabase()) {
                    log.info("load: database: " + database.getName());
                    oiyoInfo.getSettings().getDatabase().add(database);
                }
                for (OiyoSettingsEntitySet entitySet : loadedSettings.getEntitySet()) {
                    log.info("load: entitySet: " + entitySet.getName());
                    oiyoInfo.getSettings().getEntitySet().add(entitySet);
                }
            } catch (IOException ex) {
                // [M024] UNEXPECTED: Fail to load Oiyokan settings
                log.error(OiyokanMessages.IY7112 + ": " + ex.toString());
                // しかし例外は発生させず処理続行。
            }
        }

        try {
            for (String[] sqlFileDef : OIYOKAN_FILE_SQLS) {
                log.info("OData: load: internal db:" + sqlFileDef[0] + ", sql: " + sqlFileDef[1]);

                OiyoSettingsDatabase lookDatabase = OiyoInfoUtil.getOiyoDatabaseByName(oiyoInfo, sqlFileDef[0]);

                try (Connection connLoookDatabase = OiyoCommonJdbcUtil.getConnection(lookDatabase)) {
                    final String[] sqls = OiyokanResourceSqlUtil.loadOiyokanResourceSql("oiyokan/sql/" + sqlFileDef[1]);
                    for (String sql : sqls) {
                        try (var stmt = connLoookDatabase.prepareStatement(sql.trim())) {
                            stmt.executeUpdate();
                            connLoookDatabase.commit();
                        } catch (SQLException ex) {
                            log.error("UNEXPECTED: Fail to execute SQL for local internal table(2): " + ex.toString());
                            throw new ODataApplicationException(
                                    "UNEXPECTED: Fail to execute SQL for local internal table(2)", 500, Locale.ENGLISH);
                        }
                    }

                    log.info("OData: load: internal db: end: " + sqlFileDef[0] + ", sql: " + sqlFileDef[1]);
                } catch (SQLException ex) {
                    log.error("UNEXPECTED: Fail to execute Dabaase: " + ex.toString());
                    throw new ODataApplicationException("UNEXPECTED: Fail to execute Dabaase", 500, Locale.ENGLISH);
                }
            }
        } catch (ODataApplicationException ex) {
            // とめる。
        }

        return oiyoInfo;
    }
}
