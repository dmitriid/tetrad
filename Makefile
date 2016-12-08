all:
	docker build --rm=false -t tetrad-build -f Dockerfile.build .
	docker run --rm=false -it -v ${HOME}/.m2:/root/.m2 -v ${PWD}:/tetrad/src tetrad-build
	docker build --rm=false -t tetrad -f Dockerfile.run .
run:
	docker run -dt -v ${TETRAD_CONFIG}:/tetrad/config -v ${PWD}/logs:/var/log/tetrad tetrad
