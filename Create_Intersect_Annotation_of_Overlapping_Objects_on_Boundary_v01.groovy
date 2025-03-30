// Import necessary libraries
import qupath.lib.objects.classes.PathClassFactory
import java.awt.geom.Area
import org.locationtech.jts.geom.Geometry
import qupath.lib.common.GeneralTools
import qupath.lib.objects.PathObject
import qupath.lib.objects.PathObjects
import qupath.lib.roi.GeometryTools
import qupath.lib.roi.ROIs


//
resolveHierarchy()

// Define class names
String LN_CLASS_NAME = 'LN'
String GC_CLASS_NAME = 'GC'

// Retrieve annotations
def annotations = getAnnotationObjects()

// Initialize list to store GC annotations that are not inside an LN
def gcAnnotationsNotInLn = []

// Initialize map to store LN ROIs for intersection checks
def lnMap = [:]

// Fetch hierarchy to add objects later
def hierarchy = getCurrentHierarchy()

for (def annotation : annotations) {
    def pathClass = annotation.getPathClass()

    // Continue to the next iteration if this annotation does not have a PathClass
    if (pathClass == null) continue

    // If the annotation is LN, add its ROI to the map
    if (pathClass.getName() == LN_CLASS_NAME) {
        lnMap[annotation] = new Area(annotation.getROI().getShape())
    }

    // Check if the annotation is a GC
    else if (pathClass.getName() == GC_CLASS_NAME) {
        // Check if the GC is inside an LN
        def parent = annotation.getParent()
        while (parent != null) {
            def parentClass = parent.getPathClass()
            if (parentClass != null && parentClass.getName() == LN_CLASS_NAME) {
                // If the GC is inside an LN, skip to the next iteration
                break
            }
            parent = parent.getParent()
        }

        // If the loop finished without finding an LN parent, add the GC to the list
        if (parent == null) {
            gcAnnotationsNotInLn.add(annotation)
        }
    }
}

// Iterate over the list of GCs not in LN to check for intersections with LN ROIs
gcAnnotationsNotInLn.each { gc ->
    def gcArea = new Area(gc.getROI().getShape())
    def gcGeometry = gc.getROI().getGeometry()
    lnMap.each { ln, lnArea ->
        def lnGeometry = ln.getROI().getGeometry()
        def intersectionArea = new Area(gcArea)
        intersectionArea.intersect(lnArea)
        if (!intersectionArea.isEmpty()) {
            // Find the intersection of the LN and GC geometries
            def intersectionGeometry = gcGeometry.intersection(lnGeometry)

            // Convert the intersection geometry to an ROI
            def plane = gc.getROI().getImagePlane()
            def intersectRoi = GeometryTools.geometryToROI(intersectionGeometry, plane)

            // Create a new GC annotation from the intersection ROI
            def intersectAnnotation = PathObjects.createAnnotationObject(intersectRoi, gc.getPathClass())
            intersectAnnotation.setName("Intersection")

            // Add the intersection GC as a child of the LN in the hierarchy
            hierarchy.addObjectBelowParent(ln, intersectAnnotation, true)
        }
    }
}

// Update hierarchy after moving annotations
fireHierarchyUpdate()



println("Moved " + gcAnnotationsNotInLn.size() + " GC annotations that were intersecting with an LN into the LN's hierarchy")
