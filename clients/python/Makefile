.PHONY: help bootstrap-test
.DEFAULT_GOAL := help

help:
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

bootstrap: ## bootstrap test environment
	pip install -r test-requirements.txt
	
test: ## Run test cases
	python -m pytest --ignore=tests/integration

coverage-report: ## Generate test coverage report
	coverage run --source=. -m pytest --ignore=tests/integration
	coverage report -m
	