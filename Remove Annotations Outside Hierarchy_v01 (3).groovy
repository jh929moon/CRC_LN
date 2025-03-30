import qupath.lib.objects.classes.PathClassFactory

// Define class names
String LN_CLASS_NAME = 'LN'
String GC_CLASS_NAME = 'GC'

// Retrieve annotations
def annotations = getAnnotationObjects()

// Initialize list to store GC annotations that are not inside an LN
def gcAnnotationsNotInLn = []

for (def annotation : annotations) {
    def pathClass = annotation.getPathClass()
    
    // Continue to the next iteration if this annotation does not have a PathClass
    if (pathClass == null) continue
    
    // Check if the annotation is a GC
    if (pathClass.getName() == GC_CLASS_NAME) {
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

// Delete GC annotations that are not inside an LN
removeObjects(gcAnnotationsNotInLn, true)
println("Removed " + gcAnnotationsNotInLn.size() + " GC annotations that were not inside an LN")
