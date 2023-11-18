package com.bhola.livevideochat4.Models;


public class GalleryModel {

    String downloadUrl, image_uri, imagePathFirebaseStorage,imageFileNAme;

    public GalleryModel() {
    }

    public GalleryModel(String downloadUrl, String image_uri, String imagePathFirebaseStorage, String imageFileNAme) {
        this.downloadUrl = downloadUrl;
        this.image_uri = image_uri;
        this.imagePathFirebaseStorage = imagePathFirebaseStorage;
        this.imageFileNAme = imageFileNAme;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public String getImagePathFirebaseStorage() {
        return imagePathFirebaseStorage;
    }

    public void setImagePathFirebaseStorage(String imagePathFirebaseStorage) {
        this.imagePathFirebaseStorage = imagePathFirebaseStorage;
    }

    public String getImageFileNAme() {
        return imageFileNAme;
    }

    public void setImageFileNAme(String imageFileNAme) {
        this.imageFileNAme = imageFileNAme;
    }
}
