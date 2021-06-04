package vision.chain.silpo;

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

public class SilpoParser {
    private static final List<String> CONTENT_END_SYMBOLS = new ArrayList<>();
    private static final Predicate<String> X_PREDICATE = s -> s.contains("X") && s.length() < 10;
    private static final Predicate<String> REMOVE_PREDICATE = s -> !s.contains(" A") && s.contains(",") && s.length() < 7;
    private static final Predicate<String> REMOVE_SYMBOLS_PREDICATE = s -> s.contains(" x") && s.contains(",");
    private static final Predicate<String> DISCOUNT_PREDICATE = s -> !s.contains("ЗНИ");

    static {
        CONTENT_END_SYMBOLS.add("Спец");
        CONTENT_END_SYMBOLS.add("акції");
        CONTENT_END_SYMBOLS.add("Сkap");
        CONTENT_END_SYMBOLS.add("ДЛЯ ВАС");
    }

    public static Map<String, String> parseSilpoChain(List<String> lines) {
        lines = removeFromItemsByPredicate(lines, X_PREDICATE.negate());

        Iterator<String> iterator = lines.iterator();

        moveIteratorToGivenSymbols(iterator);

        if (!iterator.hasNext()) {
            return new HashMap<>();
        }

        List<String> content = extractContentTillSymbols(iterator, CONTENT_END_SYMBOLS);
        if (content.isEmpty()) {
            return new HashMap<>();
        }

        List<String> prices = extractPrices(content);

        content.removeAll(prices);

        content = removeFromItemsByPredicate(content, REMOVE_PREDICATE.negate());
        content = removeFromItemsByPredicate(content, DISCOUNT_PREDICATE);
        content = removeFromItemsByPredicate(content, REMOVE_SYMBOLS_PREDICATE.negate());

        return extractItemPerPriceMap(content, prices);
    }
}
