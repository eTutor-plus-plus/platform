package at.jku.dke.etutor.service.dto.dispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLSchemaInfoDTO {
    private Map<String, List<String>> tableColumns;
    private int diagnoseConnectionId;

    public SQLSchemaInfoDTO(){
        diagnoseConnectionId = -1;
        tableColumns = new HashMap<>();
    }

    public Map<String, List<String>> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(Map<String, List<String>> tableColumns) {
        this.tableColumns = tableColumns;
    }

    public int getDiagnoseConnectionId() {
        return diagnoseConnectionId;
    }

    public void setDiagnoseConnectionId(int diagnoseConnectionId) {
        this.diagnoseConnectionId = diagnoseConnectionId;
    }
}
