package me.tj3828.restapiwithspring.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.validation.Errors;

import java.io.IOException;

/**
 * @author tj3828
 */

@JsonComponent
public class ErrorsSerializer extends JsonSerializer<Errors> {


    @Override
    public void serialize(Errors errors, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartArray();
        errors.getFieldErrors().forEach(e -> {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("field", e.getField());
                jsonGenerator.writeStringField("objectName", e.getObjectName());
                jsonGenerator.writeStringField("code", e.getCode());
                jsonGenerator.writeStringField("defaultMessage", e.getDefaultMessage());
                Object rejectValue = e.getRejectedValue();
                if (rejectValue != null) {
                    jsonGenerator.writeStringField("rejectedValue", rejectValue.toString());
                }
                jsonGenerator.writeEndObject();
            }catch (IOException e1) {
                e1.printStackTrace();;
            }

        });

        errors.getGlobalErrors().forEach(e -> {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("objectName", e.getObjectName());
                jsonGenerator.writeStringField("code", e.getCode());
                jsonGenerator.writeStringField("defaultMessage", e.getDefaultMessage());
                jsonGenerator.writeEndObject();
            }catch (IOException e1) {
                e1.printStackTrace();;
            }
        });
        jsonGenerator.writeEndArray();

    }
}


