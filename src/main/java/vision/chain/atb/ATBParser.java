package vision.chain.atb;

import io.vavr.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static vision.util.CheckUtils.extractContentWithPrices;
import static vision.util.CheckUtils.extractItemPerPriceMap;
import static vision.util.CheckUtils.extractMatchedSymbols;
import static vision.util.CheckUtils.removeFromItemsByPredicate;

public class ATBParser {
    private static final List<String> MATCH_SYMBOLS = new ArrayList<>();
    private static final List<String> CONTENT_END_SYMBOLS = new ArrayList<>();
    private static final Predicate<String> SALES_PREDICATE = s -> s.contains("-");

    static {
        MATCH_SYMBOLS.add("Паk");
        MATCH_SYMBOLS.add("Пак");
        MATCH_SYMBOLS.add("Nak");
        MATCH_SYMBOLS.add(" г");
        MATCH_SYMBOLS.add(" л ");
        MATCH_SYMBOLS.add("гат");
        MATCH_SYMBOLS.add(" кг ");
        MATCH_SYMBOLS.add(" мл ");
        MATCH_SYMBOLS.add(" мм ");
        MATCH_SYMBOLS.add(" kr ");
        MATCH_SYMBOLS.add(" шт");
        MATCH_SYMBOLS.add("Ci");
        MATCH_SYMBOLS.add("Дес");
        MATCH_SYMBOLS.add(" o/п ");
        MATCH_SYMBOLS.add(" ф/п ");

        CONTENT_END_SYMBOLS.add("CUMA");
        CONTENT_END_SYMBOLS.add("СУМА");
        CONTENT_END_SYMBOLS.add("СУNА");
        CONTENT_END_SYMBOLS.add("CYMA");
        CONTENT_END_SYMBOLS.add("CUMA");
        CONTENT_END_SYMBOLS.add("CYNA");
    }

    public static Map<String, String> parseATBChain(List<String> lines) {
        Tuple2<List<String>, List<String>> contentWithPrices = extractContentWithPrices(lines, CONTENT_END_SYMBOLS);

        List<String> content = contentWithPrices._1;
        List<String> prices = contentWithPrices._2;

        if (content.isEmpty() || prices.isEmpty()) {
            return new HashMap<>();
        }

        prices = removeFromItemsByPredicate(prices, SALES_PREDICATE.negate());

        List<String> matchedSymbols = extractMatchedSymbols(content, MATCH_SYMBOLS);

        return extractItemPerPriceMap(matchedSymbols, prices);
    }
}
