package edu.up.cg.integrations.metadata;

public final class GeoPoint {
	private final double latitude;
	private final double longitude;

	public GeoPoint(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
}
