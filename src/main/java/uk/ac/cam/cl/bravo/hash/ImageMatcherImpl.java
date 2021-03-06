package uk.ac.cam.cl.bravo.hash;

import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class ImageMatcherImpl implements Serializable, ImageMatcher {

    private HashMap<Pair<Bodypart, Integer>, ImageHasher> normalHashers;
    private HashMap<Pair<Bodypart, Integer>, ImageHasher> abnormalHashers;

    @Override
    @NotNull
    public List<Pair<File, Integer>> findMatchingImage(@NotNull BufferedImage image,
                                                       @NotNull BoneCondition boneCondition,
                                                       @NotNull BodypartView bodypartView, int n, boolean useClusters) {

        if (useClusters) {

            ImageHasher imageHasher;

            if (boneCondition.getLabel().equals("positive")){
                imageHasher = abnormalHashers.get(new Pair<>(bodypartView.getBodypart(), bodypartView.getValue()));
            } else {
                imageHasher = normalHashers.get(new Pair<>(bodypartView.getBodypart(), bodypartView.getValue()));
            }

            return imageHasher.getNPairs(image, n);

        } else {

            List<Pair<File, Integer>> matches = new ArrayList<>();

            if (boneCondition.getLabel().equals("positive")){
                for (ImageHasher hasher : abnormalHashers.values()){
                    matches.addAll(hasher.getNPairs(image, n));
                }
            } else {
                for (ImageHasher hasher : normalHashers.values()){
                    matches.addAll(hasher.getNPairs(image, n));
                }
            }

            matches.sort(new Comparator<>() {
                @Override
                public int compare(Pair<File, Integer> o1, Pair<File, Integer> o2) {
                    return o1.getValue() - o2.getValue();
                }
            });

            return matches.subList(0,n);

        }
    }

    public static ImageMatcherImpl getImageMatcher(File f){
        try {
            FileInputStream fileInputStream = new FileInputStream(f);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            return (ImageMatcherImpl) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void trainImageMatcher(File f, Dataset dataset){
        try{
            ImageMatcherImpl imageMatcher = new ImageMatcherImpl(dataset);
            f.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            ObjectOutputStream objOut = new ObjectOutputStream(fileOutputStream);
            objOut.writeObject(imageMatcher);
            objOut.close();
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
    }

    private ImageMatcherImpl(Dataset dataset){
        normalHashers = new HashMap<>();
        abnormalHashers = new HashMap<>();

        Map<String, ImageSample> trainingMap = dataset.getTraining();


        ImageSample imageSample;
        BodypartView bodypartView;
        BoneCondition boneCondition;
        ImageHasher imageHasher;

        for (String path : trainingMap.keySet()){
            imageSample = trainingMap.get(path);
            bodypartView = imageSample.getBodypartView();
            boneCondition = imageSample.getBoneCondition();
            HashMap<Pair<Bodypart, Integer>, ImageHasher> hasherMap;

            if (boneCondition.getLabel().equals("positive")){
                hasherMap = abnormalHashers;
            } else {
                hasherMap = normalHashers;
            }

            imageHasher = hasherMap.getOrDefault(new Pair<>(bodypartView.getBodypart(), bodypartView.getValue()), null);

            if (imageHasher == null){
                imageHasher = new ImageHasher();
                hasherMap.put(new Pair<>(bodypartView.getBodypart(), bodypartView.getValue()), imageHasher);
            }

            imageHasher.addImageFile(new File(path));
        }
    }
}
