package vision.util;

import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CheckUtils {
    private static final List<String> PRICE_SYMBOLS = new ArrayList<>();
    private static final List<String> CONTENT_START_SYMBOLS = new ArrayList<>();

    static {
        PRICE_SYMBOLS.add("A");
        PRICE_SYMBOLS.add("À");

        CONTENT_START_SYMBOLS.add("Чек");
        CONTENT_START_SYMBOLS.add("Чеk");
        CONTENT_START_SYMBOLS.add("YEK");
        CONTENT_START_SYMBOLS.add("Yek");
        CONTENT_START_SYMBOLS.add("ЧЕК");
        CONTENT_START_SYMBOLS.add("EK");
        CONTENT_START_SYMBOLS.add("чек");
        CONTENT_START_SYMBOLS.add("yok");
    }

    public static List<String> removeSingleItems(List<String> items) {
        return items.stream()
                .filter(s -> s.length() > 1)
                .collect(Collectors.toList());
    }

    public static List<String> removeFromItemsByPredicate(List<String> allItems, Predicate<String> predicate) {
        return allItems.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public static List<String> extractPrices(List<String> items) {
        List<String> prices = new ArrayList<>();

        for (String item : items) {
            if ((item.contains(" А") || item.contains(" A") || item.contains(" À") || item.contains("fi") || item.contains(" в")) && (item.contains(",") || item.contains("."))) {
                if (item.length() == 6 || item.length() == 7 || item.length() == 8) {
                    prices.add(item);
                    continue;
                }
                if (isPresentLatterAtExpectedPosition(item)) {
                    prices.add(item);
                } else {
                    String substring = item.substring((item.length()) - 8);
                    if (isPresentLatterAtExpectedPosition(substring)) {
                        prices.add(substring);
                    }
                }
            }
        }

        return prices.stream()
                .filter(CheckUtils::containsOnlyLatterA)
                .collect(Collectors.toList());
    }

    private static boolean containsOnlyLatterA(String line) {
        for (char c : line.toCharArray()) {
            if (!Character.isAlphabetic(c)) {
                continue;
            }
            return ('A' == c) || ('в' == c);
        }

        return true;
    }

    private static boolean isPresentLatterAtExpectedPosition(String item) {
        return PRICE_SYMBOLS.stream().anyMatch(s -> s.equals(item.substring(6, 7))) ||
                PRICE_SYMBOLS.stream().anyMatch(s -> s.equals(item.substring(5, 6))) ||
                PRICE_SYMBOLS.stream().anyMatch(s -> s.equals(item.substring(7, 8)));
    }



    public static void moveIteratorToGivenSymbols(Iterator<String> iterator) {
        while (iterator.hasNext()) {
            String line = iterator.next();

            if (CONTENT_START_SYMBOLS.stream().anyMatch(line::contains)) {
                break;
            }
        }
    }

    public static List<String> extractContentTillSymbols(Iterator<String> iterator, List<String> tillSymbols) {
        List<String> items = new ArrayList<>();

        while (iterator.hasNext()) {
            String line = iterator.next();

            if (tillSymbols.stream().anyMatch(s -> line.equals(s) || line.contains(s))) {
                break;
            }

            if (!iterator.hasNext()) {
                return Collections.emptyList();
            }

            items.add(line);
        }

        return items;
    }

    public static List<String> extractMatchedSymbols(List<String> items, List<String> symbolsToMatch) {
        Iterator<String> iterator = items.iterator();

        List<String> itemNames = new ArrayList<>();

        while (iterator.hasNext()) {
            String line = iterator.next();

            if (symbolsToMatch.stream().anyMatch(line::contains)) {
                itemNames.add(line);
            }
        }

        return itemNames;
    }

    public static Map<String, String> extractItemPerPriceMap(List<String> matchedSymbols, List<String> prices) {
        Map<String, String> itemPerPrice = new HashMap<>();

        if (matchedSymbols.size() > prices.size()) {
            int size = matchedSymbols.size();
            for (int i = prices.size(); i < size; i++) {
                prices.add("Not Found");
            }
        } else if (prices.size() > matchedSymbols.size()) {
            int size = prices.size();
            for (int i = matchedSymbols.size(); i < size; i++) {
                matchedSymbols.add("Not Found");
            }
        }

        int counter = 0;
        for (String matchedItem : matchedSymbols) {
            itemPerPrice.put(matchedItem, prices.get(counter++));
        }

        return itemPerPrice;
    }

    public static Tuple2<List<String>, List<String>> extractContentWithPrices(List<String> lines, List<String> contentEndSymbols) {
        Iterator<String> iterator = lines.iterator();

        moveIteratorToGivenSymbols(iterator);

        if (!iterator.hasNext()) {
            return Tuple.of(Collections.emptyList(), Collections.emptyList());
        }

        List<String> content = extractContentTillSymbols(iterator, contentEndSymbols);

        List<String> prices = extractPrices(content);

        return Tuple.of(content, prices);
    }

    public static List<String> extractContent(List<String> lines, List<String> contentEndSymbols) {
        Iterator<String> iterator = lines.iterator();

        moveIteratorToGivenSymbols(iterator);

        if (!iterator.hasNext()) {
            return Collections.emptyList();
        }

        return extractContentTillSymbols(iterator, contentEndSymbols);
    }

}
