package at.jku.dke.etutor.service.dto.dispatcher;

public class DispatcherXMLDTO {
    private String diagnoseXML;
    private String submissionXML;

    public DispatcherXMLDTO(){

    }

    public DispatcherXMLDTO(String diagnoseXML, String submissionXML){
        this.diagnoseXML = diagnoseXML;
        this.submissionXML = submissionXML;
    }

    public String getDiagnoseXML() {
        return diagnoseXML;
    }

    public void setDiagnoseXML(String diagnoseXML) {
        this.diagnoseXML = diagnoseXML;
    }

    public String getSubmissionXML() {
        return submissionXML;
    }

    public void setSubmissionXML(String submissionXML) {
        this.submissionXML = submissionXML;
    }
}
