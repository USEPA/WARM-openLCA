WARM-openLCA

## Building the distribution packages
To build the distribution packages, we currently use the standard PDE Export
wizard. Click on the `gov.epa.warm` project and then on `Export...` from the context
menu. Select `Plug-in Development > Eclipse Product` from the export wizard and
select the following options in the export dialog:

* Configuration: `/gov.epa.warm/WARM.product` (should be the default)
* Root directory: `WARM`
* Synchronize before exporting: yes [x]
* Destination directory: choose the `gov.epa.warm/build` folder of this project
* Generate p2 repository: no [ ] (would be just overhead)
* Export for multiple platforms: yes [x]
* (take the defaults for the others)

In the next page, select the platforms for which you want to build the product.
After the export, you need to run the package script `make.py` to copy
resources like the Java runtime, the native math libraries, etc. to the
application folder and to create the installers.

The packager script can build distribution packages for the following platforms
(but you do not need to build them all, if a platform product is missing it is
simply ignored in the package script):

* Linux gtk x86_64
* macOS cocoa x86_64
* Windows win32 x86_64
