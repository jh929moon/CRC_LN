import qupath.lib.common.GeneralTools
import java.nio.file.Files
import java.nio.file.Paths

def imageData = getCurrentImageData()
def server = imageData.getServer()
// ',' 문자를 '_' 문자로 대체
def imageName = server.getMetadata().getName().replace(',', '_')

// LN 객체들을 모두 가져옵니다.
def LNObjects = getObjects { p -> p.getPathClass().toString() == "LN" }
def LN_count = LNObjects.size() // LN 객체의 개수를 계산합니다.

// Pixel calibration
def pixelCalibration = server.getPixelCalibration()
def pixelWidth = pixelCalibration.getPixelWidthMicrons()
def pixelHeight = pixelCalibration.getPixelHeightMicrons()

def csvOutput = new StringBuilder()
csvOutput.append("Image, LN_count, LN_area, LN_perimeter, LN_max_diameter, Tumor_count, Tumor_area_mean, Tumor_area_max, Tumor_area_sum, GC_count, GC_area_mean, GC_area_max, GC_area_sum, GC_circularity_mean, GC_solidity_mean, GC_Max_diameter_mean, GC_Max_diameter_sum, GC_Max_diameter_MAX, GC_max_perimeter, GC_perimeter_sum\n")

LNObjects.each { lnObject ->
    def tumorAreas = []
    def gcAreas = []
    def gcCircularities = []
    def gcSolidities = []
    def gcMaxDiameters = []
    def gcPerimeters = []

    // Bounding box를 통해 최대 직경 계산
    def envelope = lnObject.getROI().getGeometry().getEnvelopeInternal()
    def maxWidth = envelope.getWidth() * pixelWidth / 1000  // Convert from µm to mm
    def maxHeight = envelope.getHeight() * pixelHeight / 1000  // Convert from µm to mm
    def maximalDiameter = Math.sqrt(maxWidth * maxWidth + maxHeight * maxHeight)  // Calculate the diagonal in mm

    def detections = lnObject.getChildObjects()

    detections.each { detection ->
        def area = detection.getMeasurementList().getMeasurementValue("Area µm^2")
        def circularity = detection.getMeasurementList().getMeasurementValue("Circularity")
        def solidity = detection.getMeasurementList().getMeasurementValue("Solidity")
        def maxDiameter = detection.getMeasurementList().getMeasurementValue("Max diameter µm")

        if (detection.getPathClass().toString() == "Tumor") {
            tumorAreas.add(area)
        } else if (detection.getPathClass().toString() == "GC") {
            gcAreas.add(area)
            gcCircularities.add(circularity)
            gcSolidities.add(solidity)
            gcMaxDiameters.add(maxDiameter)
            gcPerimeters.add(detection.getROI().getGeometry().getLength() * ((pixelWidth + pixelHeight) / 2) / 1000) // GC perimeter in mm
        }
    }

    // Calculate the area and perimeter of LN in mm²
    def LN_area = (lnObject.getROI().getArea() * pixelWidth * pixelHeight) / 1e6
    def LN_perimeter = lnObject.getROI().getGeometry().getLength() * ((pixelWidth + pixelHeight) / 2) / 1000 // LN Perimeter in mm

    def tumor_count = tumorAreas.size() ?: 0
    def tumor_area_mean = tumor_count > 0 ? tumorAreas.sum() / tumor_count : 0
    def tumor_area_max = tumorAreas.max() ?: 0
    def tumor_area_sum = tumorAreas.sum() ?: 0

    def gc_count = gcAreas.size() ?: 0
    def gc_area_mean = gc_count > 0 ? gcAreas.sum() / gc_count : 0
    def gc_area_max = gcAreas.max() ?: 0
    def gc_area_sum = gcAreas.sum() ?: 0
    def gc_circularity_mean = gc_count > 0 ? gcCircularities.sum() / gc_count : 0
    def gc_solidity_mean = gc_count > 0 ? gcSolidities.sum() / gc_count : 0
    def gc_Max_diameter_mean = gc_count > 0 ? gcMaxDiameters.sum() / gc_count : 0
    def gc_Max_diameter_sum = gcMaxDiameters.sum() ?: 0
    def gc_Max_diameter_MAX = gcMaxDiameters.max() ?: 0
    def gc_max_perimeter = gcPerimeters.max() ?: 0
    def gc_perimeter_sum = gcPerimeters.sum() ?: 0

    csvOutput.append("$imageName, $LN_count, $LN_area, $LN_perimeter, $maximalDiameter, $tumor_count, $tumor_area_mean, $tumor_area_max, $tumor_area_sum, $gc_count, $gc_area_mean, $gc_area_max, $gc_area_sum, $gc_circularity_mean, $gc_solidity_mean, $gc_Max_diameter_mean, $gc_Max_diameter_sum, $gc_Max_diameter_MAX, $gc_max_perimeter, $gc_perimeter_sum\n")
}

def PROJECT_BASE_DIR = getProject().getBaseDirectory().toString()
def outputPath = buildFilePath(PROJECT_BASE_DIR, 'Results_Detection_0514', "${imageName}.csv")

try {
    Files.createDirectories(Paths.get(PROJECT_BASE_DIR, 'Results_Detection_0514'))
} catch (Exception e) {
    print("Error creating directories: ${e.getMessage()}")
}

Files.write(Paths.get(outputPath), csvOutput.toString().getBytes())
