package hu.exercise.spring.kafka.input;

import java.math.BigDecimal;
import java.util.Currency;

import com.univocity.parsers.conversions.Conversion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class MonetaryAmount implements Conversion<String, MonetaryAmount> {

	@PositiveOrZero
	private BigDecimal amount;

	// ISO 4217
	@NotNull(message = "currency is required")
	private Currency currency;

	@Override
	public MonetaryAmount execute(String input) {

		if (input == null) {
			return null;
		}
		if (input.indexOf(" ") < 0) {
			return null;
		}

		input = input.replaceAll("^\"|\"$", "");

		String substring1 = input.substring(0, input.indexOf(" "));
		this.amount = new BigDecimal(substring1);
		String substring2 = input.substring(input.indexOf(" ") + 1);
		this.currency = Currency.getInstance(substring2);
		return this;
	}

	@Override
	public String revert(MonetaryAmount input) {
		if (input == null) {
			return null;
		}
		return input.getAmount() + " " + input.getCurrency();
	}

}
