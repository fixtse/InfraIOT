#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 || ! $1 =~ ^[A-Za-z][A-Za-z0-9_-]*$ ]]; then
    echo "Usage: $0 PROJECT_NAME" >&2
    echo "Use a letter first, followed by letters, numbers, underscores or hyphens." >&2
    exit 1
fi

project_name=$1
template_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd -P)
webapps_dir=$(dirname -- "$template_dir")
project_dir="$webapps_dir/$project_name"

# mkdir without -p refuses to overwrite an existing project.
mkdir -- "$project_dir"
mkdir -p -- "$project_dir/WEB-INF/classes" "$project_dir/.vscode"

sed -e "s|<groupId>local.saludador</groupId>|<groupId>local.$project_name</groupId>|" \
    -e "s|<artifactId>saludador</artifactId>|<artifactId>$project_name</artifactId>|" \
    -e "s|<finalName>saludador</finalName>|<finalName>$project_name</finalName>|" \
    "$template_dir/pom.xml" > "$project_dir/pom.xml"

for config in settings.json tasks.json extensions.json; do
    cp -- "$template_dir/.vscode/$config" "$project_dir/.vscode/$config"
done

cat > "$project_dir/.gitignore" <<'EOF'
/target/
/.vscode/java-bin/
/WEB-INF/classes/*.class
EOF

cat > "$project_dir/WEB-INF/web.xml" <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee" version="6.0">
</web-app>
EOF

cat > "$project_dir/WEB-INF/classes/HolaServlet.java" <<'EOF'
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/hola")
public class HolaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/plain; charset=UTF-8");
        resp.getWriter().println("Hola desde " + req.getContextPath());
    }
}
EOF

cat > "$project_dir/index.html" <<EOF
<!DOCTYPE html>
<html lang="es">
<head><meta charset="UTF-8"><title>$project_name</title></head>
<body><h1>$project_name</h1><a href="hola">Abrir servlet</a></body>
</html>
EOF

printf 'Created project: %s\n' "$project_dir"
printf 'Open this folder in VS Code and use Ctrl+Shift+B to compile.\n'
printf 'Or run: tomcat-compile %q\n' "$project_dir"
printf 'After compiling, visit: http://localhost:8080/%s/hola\n' "$project_name"
