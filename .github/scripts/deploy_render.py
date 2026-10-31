"""Deploy only the tested commit; never print tokens, API response bodies or logs."""
import json
import os
import time
import urllib.error
import urllib.parse
import urllib.request


def run():
    token = os.environ['RENDER_API_KEY']
    service = os.environ['RENDER_SERVICE_ID']
    commit = os.environ['GITHUB_SHA']
    base = os.environ['RENDER_BACKEND_URL'].rstrip('/')
    url = urllib.parse.urlsplit(base)
    if (url.scheme != 'https' or not url.hostname or url.username or url.password
            or url.path or url.query or url.fragment
            or url.hostname in {'localhost', '127.0.0.1', '0.0.0.0', '::1'}):
        raise RuntimeError('RENDER_BACKEND_URL must be a public HTTPS origin.')
    if not service.startswith('srv-') or not service.replace('-', '').isalnum():
        raise RuntimeError('Invalid Render service ID.')

    def api(path, payload=None):
        request = urllib.request.Request(
            'https://api.render.com/v1/' + path,
            data=None if payload is None else json.dumps(payload).encode(),
            headers={'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json'})
        with urllib.request.urlopen(request, timeout=30) as response:
            return json.load(response)

    deployment = api(f'services/{service}/deploys', {'commitId': commit, 'clearCache': 'do_not_clear'})
    deploy_id = deployment['id']
    deadline = time.monotonic() + 1200
    while time.monotonic() < deadline:
        deployment = api(f'services/{service}/deploys/{deploy_id}')
        status = deployment['status']
        if status == 'live':
            if deployment.get('commit', {}).get('id') != commit:
                raise RuntimeError('Render live commit does not match the checked commit.')
            with urllib.request.urlopen(base + '/api/health', timeout=120) as response:
                if response.status != 200 or json.load(response).get('status') != 'UP':
                    raise RuntimeError('Render database readiness check failed.')
            print('Render checked commit deployed and database readiness passed.')
            return
        if status in {'build_failed', 'update_failed', 'pre_deploy_failed', 'canceled', 'deactivated'}:
            raise RuntimeError('Render deployment did not succeed: ' + status)
        time.sleep(10)
    raise RuntimeError('Render deployment readiness timed out after 20 minutes.')


if __name__ == '__main__':
    try:
        run()
    except urllib.error.HTTPError as error:
        raise SystemExit(f'Deployment HTTP request failed ({error.code}); response withheld.')
    except urllib.error.URLError:
        raise SystemExit('Deployment network request failed; connection details withheld.')
    except (KeyError, RuntimeError, ValueError) as error:
        raise SystemExit(str(error))
