package org.amfoss.paneeer.editorCamera.model;

import com.google.gson.annotations.SerializedName;

public class ItemModel {

	@SerializedName("uuid")
	public String uuid;

	@SerializedName("title")
	public String title;

	@SerializedName("description")
	public String description;

	@SerializedName("thumbnail")
	public String thumbnailUrl;

	@SerializedName("zip_file")
	public String zipFileUrl;

	@SerializedName("num_stickers")
	public int  numStickers;

	@SerializedName("num_effects")
	public int numEffects;

	@SerializedName("num_bgms")
	public int numBgms;

	@SerializedName("num_filters")
	public int numFilters;

	@SerializedName("num_masks")
	public int numMasks;

	@SerializedName("has_trigger")
	public boolean hasTrigger;

	@SerializedName("status")
	public String status;

	@SerializedName("updated_at")
	public long updatedAt;

	@SerializedName("type")
	public String type;
}
