package org.kafka.evrak.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessage {

    private MessageType messageType;
    private String offStatic;

    public String prepareErrorMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append(messageType.getMessage());
        if (this.offStatic != null) builder.append(" : ").append(offStatic);
        return builder.toString();
    }
}