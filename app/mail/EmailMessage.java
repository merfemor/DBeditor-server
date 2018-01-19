package mail;

import java.io.Serializable;

abstract class EmailMessage implements Serializable {
    private final String email;

    public EmailMessage(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    protected abstract String messageContent();
}
