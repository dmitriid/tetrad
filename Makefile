all:
	docker build -t tetrad-build -f Dockerfile.build .
	docker run -it -v ${HOME}/.m2:/root/.m2 -v ${PWD}:/tetrad/src tetrad-build
	docker build -t tetrad -f Dockerfile.run .
run:
	docker run -dt -v ${TETRAD_CONFIG}:/tetrad/config tetrad
