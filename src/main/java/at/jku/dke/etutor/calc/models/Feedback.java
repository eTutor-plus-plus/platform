package at.jku.dke.etutor.calc.models;

public class Feedback {

    private boolean isCorrect;

    private String textualFeedback;

    public Feedback(boolean isCorrect, String textualFeedback) {
        this.isCorrect = isCorrect;
        this.textualFeedback = textualFeedback;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public String getTextualFeedback() {
        return textualFeedback;
    }

    public void setTextualFeedback(String textualFeedback) {
        this.textualFeedback = textualFeedback;
    }
}
