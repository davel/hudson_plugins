Map releases = ["hardy": "8.04", "intrepid": "8.10", "jaunty": "9.04"]

["i386","amd64"].each { arch ->
    releases.keySet().each { release ->
        String dir = "target/classes/tftp/ubuntu-${release}/${arch}"
        ant.mkdir(dir:dir);

        def url = "http://us.archive.ubuntu.com/ubuntu/dists/${release}/main/installer-${arch}/current/images/netboot/ubuntu-installer/${arch}";
        ["initrd.gz","linux"].each { file ->
            ant.get(src:"${url}/${file}", dest:"${dir}/${file}", usetimestamp:true, verbose:true);
        }
    }
}

props = new Properties();
props.putAll(releases);
props.store(new FileWriter("target/classes/ubuntu-releases.properties"),null); 