package vision.chain.ashan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static vision.util.CheckUtils.extractContent;
import static vision.util.CheckUtils.extractItemPerPriceMap;
import static vision.util.CheckUtils.extractPrices;
import static vision.util.CheckUtils.removeFromItemsByPredicate;

public class AshanParser {
    private static final List<String> CONTENT_END_SYMBOLS = new ArrayList<>();
    private static final Predicate<String> DISCOUNT_PREDICATE = s -> s.contains("Зниж") || s.contains("-") || s.contains("X") || s.contains("x");

    static {
        CONTENT_END_SYMBOLS.add("Завітай");
    }

    public static Map<String, String> parseAshanChain(List<String> lines) {
        List<String> content = extractContent(lines, CONTENT_END_SYMBOLS);
        if (content.isEmpty()) {
            return new HashMap<>();
        }

        content.remove(0);

        content = removeFromItemsByPredicate(content, DISCOUNT_PREDICATE.negate());

        List<String> prices = extractPrices(content);

        content.removeAll(prices);

        return extractItemPerPriceMap(content, prices);
    }
}
