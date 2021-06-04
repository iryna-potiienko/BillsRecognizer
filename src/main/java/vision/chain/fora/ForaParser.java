package vision.chain.fora;

import io.vavr.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static vision.util.CheckUtils.extractContentWithPrices;
import static vision.util.CheckUtils.extractItemPerPriceMap;

public class ForaParser {
    private static final List<String> CONTENT_END_SYMBOLS = new ArrayList<>();

    static {
        CONTENT_END_SYMBOLS.add("КОРЕКЦІЯ");
    }

    public static Map<String, String> parseForaChain(List<String> lines) {
        Tuple2<List<String>, List<String>> contentWithPrices = extractContentWithPrices(lines, CONTENT_END_SYMBOLS);

        List<String> prices = contentWithPrices._2;
        List<String> content = contentWithPrices._1;

        if (content.isEmpty() || prices.isEmpty()) {
            return new HashMap<>();
        }

        content.removeAll(prices);

        return extractItemPerPriceMap(content, prices);
    }
}
