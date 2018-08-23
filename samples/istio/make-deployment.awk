#! /usr/bin/awk
#
# SPDX-License-Identifier: Apache-2.0
#
# Image format:
#   <prefix>/service?-<runtime>:<mp-version>
# E.g.:
# $ docker images
# REPOSITORY                   TAG                 IMAGE ID            CREATED             SIZE
# pilhuhn/serviceb-liberty     mp-1.3              0c0a0e090d19        26 minutes ago      566MB
# pilhuhn/serviceb-thorntail   mp-1.3              6c57fa7fcbd8        About an hour ago   521MB

# Usage:  awk -f make-deployment.awk [prefix=<prefix>]
#    if <prefix> is passed, only images that have this prefix will be considered.
#    e.g. with the above  prefix=pilhuhn would include the two listed images
#    while prefix=foo would not include them.
#    if no prefix is given, all images are considered


BEGIN {

	if (ARGC == 2) {
		split(ARGV[1],tmp,"=");
		PREF=tmp[2];
	}
	

	# read template
	while(getline < "deployment-template.yaml" > 0)
		template[++tlines] = $0;

	# read list of images
	while("docker images" | getline > 0 ) { 
		
		if ($0 ~ /service.-/) {
			if (index($0,PREF) != 1) {				
				continue;
			}
			# $0 is the entire line and $1..n are the columns
			# $1 = REPOSITORY column
			# $2 = TAG column
			MPVERSION=$2;			
			n = split($1, tmp, "/");
			PREFIX=tmp[1];
			SVC= tmp[2];
			n = split(SVC, tmp, "-");
			RUNTIME = tmp[2];
			SERVICE = tmp[1];
			VERSION = RUNTIME "-" MPVERSION;
		
			# print PREFIX, ">", SERVICE, ">",  RUNTIME, ">", MPVERSION, ">", VERSION;
		
			for (i = 1 ; i <= tlines; i++) {
				temp = template[i];
				gsub("#RUNTIME", RUNTIME, temp);
				gsub("#MPVERSION", MPVERSION, temp);
				gsub("#SERVICE", SERVICE, temp);
				gsub("#VERSION", VERSION, temp);
				gsub("#PREFIX", PREFIX, temp);
				gsub("#DEPLOYMENT", SERVICE"-"RUNTIME, temp);
				print temp;
				
			}
			print "---";
		}
	}
}
