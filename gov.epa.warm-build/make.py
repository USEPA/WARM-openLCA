# This script creates the warm distribution packages. It currently only
# works on Windows as it calls native binaries to create the Windows installers
# (e.g. NSIS).

import datetime
import glob
import os
import platform
import shutil
import subprocess
import stat
import tarfile

from os.path import exists


def main():
    print('Create the warm distribution packages')

    # delete the old versions
    if exists('build/dist'):
        print('Delete the old packages under build/dist', end=' ... ')
        shutil.rmtree('build/dist', ignore_errors=True)
        print('done')
    mkdir('build/dist')

    # version and time
    version = get_version()
    now = datetime.datetime.now()
    version_date = '%s_%d-%02d-%02d' % (version, now.year,
                                        now.month, now.day)

    # create packages
    pack_win(version, version_date)
    #pack_linux(version_date)
    pack_macos(version_date)

    print('All done\n')


def get_version():
    version = ''
    manifest = '../gov.epa.warm/META-INF/MANIFEST.MF'
    printw('Read version from %s' % manifest)
    with open(manifest, 'r', encoding='utf-8') as f:
        for line in f:
            text = line.strip()
            if not text.startswith('Bundle-Version'):
                continue
            version = text.split(':')[1].strip()
            break
    print("done version=%s" % version)
    return version


def get_os() -> str:
    """Get the OS on which this script is running. Returns 'linux', 'macos', or
       'windows'."""
    ps = platform.system().lower()
    if ps == 'darwin':
        return 'macos'
    if ps == "windows":
        return 'windows'
    if ps == "linux":
        return 'linux'
    return 'unknown'


def pack_win(version, version_date):
    product_dir = 'build/win32.win32.x86_64/warm'
    if not exists(product_dir):
        print('folder %s does not exist; skip Windows version' % product_dir)
        return

    print('Create Windows package')
    copy_licenses(product_dir)

    # jre
    jre_dir = p('runtime/jre/win64')
    if not exists(jre_dir):
        print('  WARNING: No JRE found %s' % jre_dir)
    else:
        if not exists(p(product_dir + '/jre')):
            printw('  Copy JRE')
            shutil.copytree(jre_dir, p(product_dir + '/jre'))
            print('done')

    # julia libs
    if not exists('runtime/julia/win64'):
        print('  WARNING: Julia libraries not found in julia/win64')
    else:
        printw("  Copy Julia libraries")
        for f in glob.glob('runtime/julia/win64/*.*'):
            shutil.copy2(p(f), product_dir)
        print('done')

    # ini config file
    ini = fill_template(p('templates/WARM_win.ini'), heap='3584M')
    with open(p(product_dir + '/WARM.ini'), 'w',
              encoding='iso-8859-1') as f:
        f.write(ini)

    # zip file
    zip_file = p('build/dist/warm_win64_' + version_date)
    printw('  Create zip %s' % zip_file)
    shutil.make_archive(zip_file, 'zip', 'build/win32.win32.x86_64/WARM')
    print('done')


def pack_linux(version_date):
    product_dir = 'build/linux.gtk.x86_64/WARM'
    if not exists(product_dir):
        print('folder %s does not exist; skip Linux version' % product_dir)
        return

    print('Create Linux package')
    copy_licenses(product_dir)

    # package the JRE
    if not exists(product_dir + '/jre'):
        jre_tar = glob.glob('runtime/jre/linux64/*linux*.tar.gz')
        if len(jre_tar) == 0:
            print('  WARNING: No Linux JRE found')
        else:
            printw('  Copy JRE')
            unzip(jre_tar[0], product_dir)
            jre_dir = glob.glob(product_dir + '/*jdk*')
            os.rename(jre_dir[0], p(product_dir + '/jre'))
            print('done')

        # ini config file
    ini = fill_template(p('templates/WARM_win.ini'), heap='3584M')
    with open(p(product_dir + '/WARM.ini'), 'w',
              encoding='iso-8859-1') as f:
        f.write(ini)

    printw('  Create distribution package')
    dist_pack = p('build/dist/openLCA_linux64_%s' % version_date)
    targz('.\\build\\linux.gtk.x86_64\\*', dist_pack)
    print('done')


def pack_macos(version_date):
    base = 'build/macosx.cocoa.x86_64/WARM'
    if not exists(base):
        print('folder %s does not exist; skip macOS version' % base)
        return
    base += "/"
    print('Create macOS package')

    printw('Move folders around')

    # os.makedirs(base + 'WARM.app/Contents/Eclipse', exist_ok=True)
    os.makedirs(base + 'WARM.app/Contents/MacOS', exist_ok=True)
    os.makedirs(base + 'WARM.app/dropins', exist_ok=True)

    shutil.copyfile(base + 'Info.plist', base + 'WARM.app/Contents/Info.plist')
    shutil.move(base + "configuration", base + 'WARM.app')
    shutil.move(base + "plugins", base + 'WARM.app')
    shutil.move(base + ".eclipseproduct", base + 'WARM.app')
    shutil.move(base + "Resources", base + "WARM.app/Contents")
    #os.chmod(base + "MacOS/WARM", stat.S_IRUSR | stat.S_IRGRP | stat.S_IROTH)
    shutil.copy(base + "MacOS/WARM", base +
                    'WARM.app/Contents/MacOS')

    # create the ini file
    plugins_dir = base + "WARM.app/plugins/"
    launcher_jar = os.path.basename(
        glob.glob(plugins_dir + "*launcher*.jar")[0])
    launcher_jar = "../../plugins/" + launcher_jar
    launcher_lib = os.path.basename(
        glob.glob(plugins_dir + "*launcher.cocoa.macosx*")[0])
    launcher_lib = "../../plugins/" + launcher_lib
    with open("templates/WARM_macos.ini", mode='r', encoding="utf-8") as f:
        text = f.read()
        text = text.format(launcher_jar=launcher_jar,
                           launcher_lib=launcher_lib)
        out_ini_path = base + "/WARM.app/Contents/MacOS/WARM.ini"
        with open(out_ini_path, mode='w', encoding='utf-8', newline='\n') as o:
            o.write(text)

    shutil.rmtree(base + "MacOS")
    os.remove(base + "Info.plist")

    # package the JRE
    if not exists(base + '/jre'):
        jre_tar = glob.glob('runtime/jre/macos64/*macos*.tar.gz')
        if len(jre_tar) == 0:
            print('  WARNING: No macos JRE found')
        else:
            printw('  Copy JRE')
            unzip(jre_tar[0], base + '/WARM.app')
            jre_dir = glob.glob(base + '/WARM.app' + '/*jdk*')
            os.rename(jre_dir[0], p(base + '/WARM.app' + '/jre'))
            print('done')

    printw('  Create distribtuion package')
    dist_pack = p('build/dist/WARM_macOS_%s' % version_date)
    targz('.\\build\\macosx.cocoa.x86_64\\WARM\\*', dist_pack)
    print('done')


def copy_licenses(product_dir: str):
    # licenses
    printw('  Copy licenses')
    if not exists(p(product_dir + '/licenses')):
        shutil.copytree(p('legal/licenses'), p(product_dir + '/licenses'))
        shutil.copy2(p('legal/LICENSE.txt'), product_dir)
        shutil.copy2(p('legal/OPENLCA_README.txt'), product_dir)
    print('done')


def mkdir(path):
    if exists(path):
        return
    try:
        os.mkdir(path)
    except Exception as e:
        print('Failed to create folder ' + path, e)


def targz(folder, tar_file):
    print('targz %s to %s' % (folder, tar_file))
    if get_os() != 'windows' or not exists('tools/7zip/7za.exe'):
        shutil.make_archive(tar_file, 'gztar', folder)
        return
    app = p('tools/7zip/7za.exe')
    cmd = [app, 'a', '-ttar', tar_file + '.tar', p(folder + '/*')]
    ps = subprocess.Popen(cmd, stdout=subprocess.PIPE)
    subprocess.call(["findstr", "/I", ''"archive everything"''], stdin=ps.stdout)
    ps.wait()
    cmd = [app, 'a', '-tgzip', tar_file + '.tar.gz', tar_file + '.tar']
    ps = subprocess.Popen(cmd, stdout=subprocess.PIPE)
    subprocess.call(["findstr", "/I", ''"archive everything"''], stdin=ps.stdout)
    ps.wait()
    os.remove(tar_file + '.tar')


def unzip(zip_file, to_dir):
    """ Extracts the given file to the given folder using 7zip."""
    print('unzip %s to %s' % (zip_file, to_dir))
    if not os.path.exists(to_dir):
        os.makedirs(to_dir)
    if get_os() != 'windows' or not exists('tools/7zip/7za.exe'):
        shutil.unpack_archive(zip_file, to_dir)
    else:
        zip_app = p('tools/7zip/7za.exe')
        # FIXME: it generates to much logs
        # 7za -y x "archive.tar.gz" -so | 7za x -aoa -si -ttar -o"archive"
        ps = subprocess.Popen([zip_app, "-y", 'x', zip_file, "-so"], stdout=subprocess.PIPE)
        code = subprocess.call([zip_app, "x", "-aoa", "-si", "-ttar", '-o%s' % to_dir], stdin=ps.stdout)
        ps.wait()
        print(code)


def move(f_path, target_dir):
    """ Moves the given file or directory to the given folder. """
    if not os.path.exists(f_path):
        # source file does not exist
        return
    base = os.path.basename(f_path)
    if os.path.exists(target_dir + '/' + base):
        # target file or dir already exsists
        return
    if not os.path.exists(target_dir):
        os.makedirs(target_dir)
    shutil.move(f_path, target_dir)


def fill_template(file_path, **kwargs):
    with open(file_path, mode='r', encoding='utf-8') as f:
        text = f.read()
        return text.format(**kwargs)


def p(path: str) -> str:
    """ Replace all occurences of '/' with os specific path separators if
        necessary. """
    if path is None:
        return ""
    p = path
    if os.sep != '/':
        p = p.replace('/', os.sep)
    return p


def printw(msg: str):
    print(msg, end=' ... ', flush=True)


if __name__ == '__main__':
    main()
