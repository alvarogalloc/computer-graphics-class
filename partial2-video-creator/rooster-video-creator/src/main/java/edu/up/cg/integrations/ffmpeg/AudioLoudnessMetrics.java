package edu.up.cg.integrations.ffmpeg;

public final class AudioLoudnessMetrics {
	private final double integratedLufs;
	private final double truePeakDbtp;
	private final double loudnessRangeLu;

	public AudioLoudnessMetrics(double integratedLufs, double truePeakDbtp, double loudnessRangeLu) {
		this.integratedLufs = integratedLufs;
		this.truePeakDbtp = truePeakDbtp;
		this.loudnessRangeLu = loudnessRangeLu;
	}

	public double integratedLufs() {
		return integratedLufs;
	}

	public double truePeakDbtp() {
		return truePeakDbtp;
	}

	public double loudnessRangeLu() {
		return loudnessRangeLu;
	}
}
