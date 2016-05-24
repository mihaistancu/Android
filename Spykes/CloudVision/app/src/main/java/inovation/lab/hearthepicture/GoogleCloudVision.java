package inovation.lab.hearthepicture;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GoogleCloudVision {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyDdxFNE6e7gWEZuzu2XEMlnHRmmy7shl1I";

    private static String TAG = "api";

    public ResultMessage Analyze(final Bitmap bitmap) {
        try {
            HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
            builder.setVisionRequestInitializer(new
                    VisionRequestInitializer(CLOUD_VISION_API_KEY));
            Vision vision = builder.build();

            BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                    new BatchAnnotateImagesRequest();
            batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                // Add the image
                Image base64EncodedImage = new Image();
                // Convert the bitmap to a JPEG
                // Just in case it's a format that Android understands but Cloud Vision
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                // Base64 encode the JPEG
                base64EncodedImage.encodeContent(imageBytes);
                annotateImageRequest.setImage(base64EncodedImage);

                // add the features we want
                annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                    Feature labelDetection = new Feature();
                    labelDetection.setType("LABEL_DETECTION");
                    labelDetection.setMaxResults(5);
                    add(labelDetection);

                    Feature faceDetection = new Feature();
                    faceDetection.setType("FACE_DETECTION");
                    add(faceDetection);

                    Feature textDetection = new Feature();
                    textDetection.setType("TEXT_DETECTION");
                    add(textDetection);

                    Feature logoDetection = new Feature();
                    logoDetection.setType("LOGO_DETECTION");
                    logoDetection.setMaxResults(5);
                    add(logoDetection);

                    Feature landmarkDetection = new Feature();
                    landmarkDetection.setType("LANDMARK_DETECTION");
                    landmarkDetection.setMaxResults(5);
                    add(landmarkDetection);
                }});

                // Add the list of one thing to the request
                add(annotateImageRequest);
            }});

            Vision.Images.Annotate annotateRequest =
                    vision.images().annotate(batchAnnotateImagesRequest);
            // Due to a bug: requests to Vision API containing large images fail when GZipped.
            annotateRequest.setDisableGZipContent(true);
            Log.d(TAG, "created Cloud Vision request object, sending request");

            BatchAnnotateImagesResponse response = annotateRequest.execute();
            return convertResponseToString(response);

        } catch (GoogleJsonResponseException e) {
            Log.d(TAG, "failed to make API request because " + e.getContent());
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }

        return null;
        //return "Cloud Vision API request failed. Check logs for details.";
    }

    private ResultMessage convertResponseToString(BatchAnnotateImagesResponse response) {
        ResultMessage messages = new ResultMessage();

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        List<FaceAnnotation> faces = response.getResponses().get(0).getFaceAnnotations();
        List<EntityAnnotation> text = response.getResponses().get(0).getTextAnnotations();
        List<EntityAnnotation> logos = response.getResponses().get(0).getLogoAnnotations();
        List<EntityAnnotation> landmarks = response.getResponses().get(0).getLandmarkAnnotations();

        if (labels != null) {
            messages.setLabels(GetAnnotationsDescription(labels, 0.5f));
        }

        if (text != null) {
            messages.setText(GetAnnotationsDescription(text, null));
        }

        if (logos != null) {
            messages.setLogo(GetAnnotationsDescription(logos, 0.1f));
        }

        if (landmarks != null) {
            messages.setLandmarks(GetAnnotationsDescription(landmarks, 0.1f));
        }

        if (faces != null) {
            String message = "";

            for (FaceAnnotation face : faces) {
                if (face.getJoyLikelihood().startsWith("VERY_LIKELY")) {
                    message += "joy, ";
                }

                if (face.getAngerLikelihood().startsWith("VERY_LIKELY")) {
                    message += "anger, ";
                }

                if (face.getSorrowLikelihood().startsWith("VERY_LIKELY")) {
                    message += "sorrow, ";
                }

                if (face.getSurpriseLikelihood().startsWith("VERY_LIKELY")) {
                    message += "surprise, ";
                }
            }

            messages.setFeelings(message);
        }

        return messages;
    }

    private String GetAnnotationsDescription(List<EntityAnnotation> annotations, Float minScore) {
        String message = "";

        for (EntityAnnotation item : annotations) {
            Float score = item.getScore();
            Float confidence = item.getConfidence();

            // daca nu are scor inseamna ca e de tip text, si vrem sa returneze doar primul rezultat de acest fel
            if (score == null || minScore == null) {
                message += item.getDescription();
                message += ", ";
                break;
            } else if (score >= minScore) {
                message += item.getDescription();
                message += ", ";
            }
        }

        return message;
    }
}
