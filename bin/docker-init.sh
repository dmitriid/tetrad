#!/usr/bin/env bash

service vernemq start && \
tmux new-session -s tetrad "tmux source-file /tetrad/bin/tetrad.tmux"
