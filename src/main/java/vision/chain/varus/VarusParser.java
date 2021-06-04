package vision.chain.varus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static vision.util.CheckUtils.extractContentTillSymbols;
import static vision.util.CheckUtils.extractItemPerPriceMap;
import static vision.util.CheckUtils.extractPrices;
import static vision.util.CheckUtils.moveIteratorToGivenSymbols;
import static vision.util.CheckUtils.removeFromItemsByPredicate;

public class VarusParser {
    private static final List<String> CONTENT_END_SYMBOLS = new ArrayList<>();
    private static final Predicate<String> DISCOUNT_PREDICATE = s -> s.contains("Зниж") || s.contains("-") || s.contains("X");

    static {
        CONTENT_END_SYMBOLS.add("Сума");
    }

    public static Map<String, String> parseVarusChain(List<String> lines) {
        Iterator<String> iterator = lines.iterator();

        moveIteratorToGivenSymbols(iterator);

        List<String> content = extractContentTillSymbols(iterator, CONTENT_END_SYMBOLS);
        if (content.isEmpty()) {
            return new HashMap<>();
        }

        content.remove(0);
        content.remove(0);
        content.remove(0);

        content = removeFromItemsByPredicate(content, DISCOUNT_PREDICATE.negate());

        List<String> prices = extractPrices(content);

        content.removeAll(prices);

        return extractItemPerPriceMap(content, prices);
    }
}
