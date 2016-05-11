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
    private static final String CLOUD_VISION_API_KEY = "AIzaSyAIle9H8YMndfaE0jGICnfIUKopfDa8o3I";
    private static String TAG = "api";

    public String Analyze(final Bitmap bitmap) {
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
                    labelDetection.setMaxResults(10);
                    add(labelDetection);

                    Feature faceDetection = new Feature();
                    faceDetection.setType("FACE_DETECTION");
                    add(faceDetection);
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
        return "Cloud Vision API request failed. Check logs for details.";
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "";

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        List<FaceAnnotation> faces = response.getResponses().get(0).getFaceAnnotations();

        if (labels != null) {
            for (EntityAnnotation label : labels) {
                //if (label.getScore() > 0.8) {
                    message += label.getDescription();
                    message += ", ";
                //}
            }

            if (message.length() > 0) {
                message = "The image contains:\n" + message.trim();

                if (message.charAt(message.length() - 1) == ',') {
                    message = message.substring(0, message.length() - 1) + ".";
                }
            }
        } else {
            message += "Sorry, nothing clear found.";
        }


        if (faces != null) {
            String feelingsMessage = "";

            for (FaceAnnotation face : faces) {
                if (face.getJoyLikelihood().startsWith("VERY_LIKELY")) {
                    feelingsMessage += "joy, ";
                }

                if (face.getAngerLikelihood().startsWith("VERY_LIKELY")) {
                    feelingsMessage += "anger, ";
                }

                if (face.getSorrowLikelihood().startsWith("VERY_LIKELY")) {
                    feelingsMessage += "sorrow, ";
                }

                if (face.getSurpriseLikelihood().startsWith("VERY_LIKELY")) {
                    feelingsMessage += "surprise, ";
                }
            }

            if (feelingsMessage.length() > 0) {
                feelingsMessage = "\nThe feelings found are: " + feelingsMessage.trim();
                message += feelingsMessage;

                if (message.charAt(message.length() - 1) == ',') {
                    message = message.substring(0, message.length() - 1) + ".";
                }
            }
        }

        return message;
    }
}
