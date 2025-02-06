SUMMARY = "Cobalt plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=${@bb.utils.contains('DISTRO_FEATURES', 'cobalt-23', '3b83ef96387f14655fc854ddc3c6bd57','a1045f140d2e71b4e089875cd5d07e42', d)}"

require starboard_revision.inc

TOOLCHAIN = "gcc"
PACKAGE_ARCH = "${APP_LAYER_ARCH}"

SRC_URI = "${STARBOARD_SRC_URI};protocol=${CMF_GIT_PROTOCOL};branch=develop"
SRC_URI:append = " file://0001-Plugin-change-to-remove-IDictionary-interface.patch;patchdir=${WORKDIR}/git"

SRCREV = "${@bb.utils.contains('DISTRO_FEATURES', 'cobalt-25', '${STARBOARD_SRCREV_25}', '${STARBOARD_SRCREV_24}', d)}"

S = "${WORKDIR}/git/plugin"

inherit cmake pkgconfig

DEPENDS = "wpeframework wpeframework-tools-native rdkservices-apis zlib"

COBALT_PERSISTENTPATHPOSTFIX ?= "Cobalt-0"
COBALT_CLIENTIDENTIFIER ?= "wst-Cobalt-0"

PACKAGECONFIG ??= "focus closurepolicy"

PACKAGECONFIG[focus]  = "-DPLUGIN_COBALT_ENABLE_FOCUS_IFACE=ON,-DPLUGIN_COBALT_ENABLE_FOCUS_IFACE=OFF,"
PACKAGECONFIG[closurepolicy]  = ",-DPLUGIN_COBALT_CLOSUREPOLICY=OFF,"
PACKAGECONFIG[evegreenlite]   = "-DPLUGIN_COBALT_EVEGREEN_LITE=ON,-DPLUGIN_COBALT_EVEGREEN_LITE=OFF,libloader-app,virtual/cobalt-evergreen"
PACKAGECONFIG[evegreencompatible]  = "-DPLUGIN_COBALT_EVEGREEN_COMPATIBLE=ON,-DPLUGIN_COBALT_EVEGREEN_COMPATIBLE=OFF,libloader-app,virtual/cobalt-evergreen"
PACKAGECONFIG[libcobalt]  = "-DPLUGIN_COBALT_EVEGREEN_LITE=OFF,,libcobalt"

def get_cobalt_config(d):
    print("DISTRO_FEATURES:", d.getVar('DISTRO_FEATURES'))
    if bb.utils.contains_any('DISTRO_FEATURES', ['libcobalt-rdm', 'libcobalt', 'morty'], True, False, d):
        print("Returning 'libcobalt'")
        return 'libcobalt'
    if bb.utils.contains('DISTRO_FEATURES', 'cobalt-evergreen-full', True, False, d):
        print("Returning 'evegreencompatible'")
        return 'evegreencompatible'
    print("Returning 'evegreencompatible evegreenlite'")
    return 'evegreencompatible evegreenlite'

PACKAGECONFIG:append = " ${@get_cobalt_config(d)}"

EXTRA_OECMAKE += " \
    -DCMAKE_BUILD_TYPE=Release \
    -DBUILD_SHARED_LIBS=ON \
    -DPLUGIN_COBALT_CLIENTIDENTIFIER="${COBALT_CLIENTIDENTIFIER}" \
    -DPLUGIN_COBALT_PERSISTENTPATHPOSTFIX="${COBALT_PERSISTENTPATHPOSTFIX}" \
"

def define_cobalt_properties(d, proplist):
    result = ''
    for prop_name in proplist.split():
        prop_name = 'PLUGIN_COBALT_PROP_' + prop_name
        prop_val = d.getVar(prop_name, True)
        if prop_val and prop_val != '':
           result += ''.join(['-D',prop_name,'=',prop_val]) + ' '
    return result

EXTRA_OECMAKE += " \
    ${@define_cobalt_properties(d,'MODELNAME MODELYEAR INTEGRATORNAME BRANDNAME FIRMWAREVERSION CHIPSETMODELNUMBER DEVICETYPE')} \
"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so"
