package vision;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageContext;
import com.google.protobuf.ByteString;
import vision.chain.ChainName;
import vision.exception.ChainNotDefinedException;
import vision.exception.ChainNotSupportedException;
import vision.exception.FailedToExtractImageTextException;
import vision.exception.FailedToInitException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static vision.chain.ashan.AshanParser.parseAshanChain;
import static vision.chain.atb.ATBParser.parseATBChain;
import static vision.chain.fora.ForaParser.parseForaChain;
import static vision.chain.silpo.SilpoParser.parseSilpoChain;
import static vision.chain.varus.VarusParser.parseVarusChain;
import static vision.util.CheckUtils.removeSingleItems;

public class ImageParser {

    private static ImageAnnotatorClient vision;

    static {
        try {
            vision = ImageAnnotatorClient.create();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> extractTextFromPhoto(File photo) throws IOException, FailedToInitException, FailedToExtractImageTextException, ChainNotDefinedException, ChainNotSupportedException {
        if (vision == null) {
            throw new FailedToInitException();
        }


//        BufferedImage image = ImageIO.read(photo);
//
//        BufferedImage result = new BufferedImage(
//                image.getWidth(),
//                image.getHeight(),
//                BufferedImage.TYPE_BYTE_BINARY);
//
//        Graphics2D graphic = result.createGraphics();
//        graphic.drawImage(image, 0, 0, Color.WHITE, null);
//        graphic.dispose();
//
//        File output = new File("temp.png");
//
//        ImageIO.write(result, "png", output);
//
//        ByteString imgBytes = prepareImageByte(output);

        ByteString imgBytes = prepareImageByte(photo);

        List<AnnotateImageRequest> requests = new ArrayList<>();

        AnnotateImageRequest request = prepareImageParseRequest(imgBytes);

        requests.add(request);

        BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
        List<AnnotateImageResponse> responses = response.getResponsesList();

        AnnotateImageResponse res = Optional.ofNullable(responses.get(0))
                .orElseThrow(RuntimeException::new);

        String imageText = extractImageText(res);
        if (imageText == null) {
            throw new FailedToExtractImageTextException(String.format("Error: %s%n", res.getError().getMessage()));
        }

        List<String> imageLines = readImageText(imageText);

        ChainName chainName = determinateChain(imageLines);

        if (ChainName.UNDEFINED.equals(chainName)) {
            throw new ChainNotDefinedException("Chain is not defined");
        }

        Map<String, String> itemPerPrice = parseImageLines(imageLines, chainName);
        if (imageLines.isEmpty()) {
            throw new ChainNotSupportedException("Not supported chain");
        }

        for (Map.Entry<String, String> stringStringEntry : itemPerPrice.entrySet()) {
            stringStringEntry.setValue(stringStringEntry.getValue().replace("A", "грн"));
        }

        return itemPerPrice;
    }

    private ByteString prepareImageByte(File file) throws IOException {
        byte[] data = Files.readAllBytes(file.toPath());
        return ByteString.copyFrom(data);
    }

    private AnnotateImageRequest prepareImageParseRequest(ByteString imgBytes) {
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();

        ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("uk").build();

        return AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .setImageContext(imageContext)
                .build();
    }

    private String extractImageText(AnnotateImageResponse res) {
        if (res.hasError()) {
            return null;
        }

        return res.getFullTextAnnotation().getText();
    }

    private List<String> readImageText(String imageText) {
        InputStream textStream = new ByteArrayInputStream(imageText.getBytes());
        Scanner scanner = new Scanner(textStream);

        List<String> lines = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lines.add(line);
        }

        return lines;
    }

    private ChainName determinateChain(List<String> lines) {
        String allLines = Stream.of(lines)
                .flatMap(Collection::stream)
                .collect(Collectors.joining());

        if (allLines.contains("АШАН")) {
            return ChainName.ASHAN;
        } else if (allLines.contains("VARUS")) {
            return ChainName.VARUS;
        } else if (allLines.contains("АТБ") || allLines.contains("AT6") || allLines.contains("Alb")) {
            return ChainName.ATB;
        } else if (allLines.contains("ФOPA") || allLines.contains("ОРА")) {
            return ChainName.FORA;
        } else if (allLines.contains("СІЛЬПО") || allLines.contains("сільпо") || allLines.contains("Сільпо")) {
            return ChainName.SILPO;
        } else {
            return ChainName.UNDEFINED;
        }
    }

    private Map<String, String> parseImageLines(List<String> lines, ChainName chain) {
        lines = removeSingleItems(lines);

        if (ChainName.FORA.equals(chain)) {
            return parseForaChain(lines);
        } else if (ChainName.ATB.equals(chain)) {
            return parseATBChain(lines);
        } else if (ChainName.SILPO.equals(chain)) {
            return parseSilpoChain(lines);
        } else if (ChainName.VARUS.equals(chain)) {
            return parseVarusChain(lines);
        } if (ChainName.ASHAN.equals(chain)){
            return parseAshanChain(lines);
        } else {
            return new HashMap<>();
        }
    }
}
