package org.amfoss.paneeer.editorCamera.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class CategoryModel {

    @SerializedName("uuid")
    public String uuid;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("is_bundle")
    public boolean isBundle;

    @SerializedName("updated_at")
    public long updatedAt;

    @SerializedName("status")
    public String status;

    @SerializedName("items")
    public List<ItemModel> items;
}
