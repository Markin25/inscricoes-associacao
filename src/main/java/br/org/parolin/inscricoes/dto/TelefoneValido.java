package br.org.parolin.inscricoes.dto;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import br.org.parolin.inscricoes.util.TelefoneUtil;

/** Bean Validation: o telefone, depois de normalizado, tem 10 ou 11 dígitos. */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TelefoneValido.Validador.class)
public @interface TelefoneValido {

    String message() default Mensagens.TELEFONE_INVALIDO;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validador implements ConstraintValidator<TelefoneValido, String> {
        @Override
        public boolean isValid(String valor, ConstraintValidatorContext context) {
            return TelefoneUtil.valido(TelefoneUtil.normalizar(valor));
        }
    }
}
