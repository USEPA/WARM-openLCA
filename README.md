# WARM-openLCA

## Loading the target platform 
The file `platform.target` in the `gov.epa.warm` project contains the definition of
the [target platform](https://help.eclipse.org/oxygen/index.jsp?topic=%2Forg.eclipse.pde.doc.user%2Fconcepts%2Ftarget.htm)
of the warm RCP application. Just open the file with the `Target Editor`
and click on `Set as target platform` on the top right of the editor.

This will download the resources of the target platform into your local
workspace and, thus, may take a while. Unfortunately, setting up and
configuring Eclipse can be quite challenging. If you get errors like
`Unable locate installable unit in target definition`,
[this discussion](https://stackoverflow.com/questions/10547007/unable-locate-installable-unit-in-target-definition)
may help. 

## Test the application
Refresh your Eclipse workspace (select all and press `F5`). Open the file
[gov.epa.warm/WARM.product](./gov.epa.warm/WARM.product) within  Eclipse and click
on the run icon inside the `WARM.product` tab. openLCA should now start.

If you want to build an installable product, see the description in the 
[gov.epa.warm-build](./gov.epa.warm-build) sub-project or simply use the Eclipse export
wizard (Export/Eclipse product). 
