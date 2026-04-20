package edu.up.cg.integrations.ffmpeg;

public final class AudioComplianceConstraints {
    public static final AudioComplianceConstraints YOUTUBE_ASSIGNMENT = new AudioComplianceConstraints(
        -16.0,
        -14.0,
        -2.0,
        -1.0,
        5.0,
        10.0,
        -15.0,
        -1.5,
        7.0
    );

    private final double minLufs;
    private final double maxLufs;
    private final double minTruePeakDbtp;
    private final double maxTruePeakDbtp;
    private final double minLraLu;
    private final double maxLraLu;
    private final double targetLufs;
    private final double targetTruePeakDbtp;
    private final double targetLraLu;

    public AudioComplianceConstraints(
        double minLufs,
        double maxLufs,
        double minTruePeakDbtp,
        double maxTruePeakDbtp,
        double minLraLu,
        double maxLraLu,
        double targetLufs,
        double targetTruePeakDbtp,
        double targetLraLu
    ) {
        this.minLufs = minLufs;
        this.maxLufs = maxLufs;
        this.minTruePeakDbtp = minTruePeakDbtp;
        this.maxTruePeakDbtp = maxTruePeakDbtp;
        this.minLraLu = minLraLu;
        this.maxLraLu = maxLraLu;
        this.targetLufs = targetLufs;
        this.targetTruePeakDbtp = targetTruePeakDbtp;
        this.targetLraLu = targetLraLu;
    }

    public double minLufs() {
        return minLufs;
    }

    public double maxLufs() {
        return maxLufs;
    }

    public double minTruePeakDbtp() {
        return minTruePeakDbtp;
    }

    public double maxTruePeakDbtp() {
        return maxTruePeakDbtp;
    }

    public double minLraLu() {
        return minLraLu;
    }

    public double maxLraLu() {
        return maxLraLu;
    }

    public double targetLufs() {
        return targetLufs;
    }

    public double targetTruePeakDbtp() {
        return targetTruePeakDbtp;
    }

    public double targetLraLu() {
        return targetLraLu;
    }
}
