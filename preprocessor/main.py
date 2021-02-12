#!/usr/bin/env python3
import os, sys, time, re

if __name__ == '__main__':

    def process(markdown_file: str, output_markdown_file: str) -> str:

        dir_of_readme = os.path.split(markdown_file)[0]
        dir_of_readme = os.path.abspath(dir_of_readme)

        is_include_re: re.Pattern[str] = re.compile(r'//\s+include:(.*)?')

        def wrap_included_contents(file_path: str, content: str) -> str:
            template = '''
            ```
            %s 
            ```
            ''' % content
            return template

        def handle_includes(line: str) -> str:
            nl: str = line.strip()
            match = is_include_re.match(nl)

            if match is not None:
                file_to_include: str = os.path.join(dir_of_readme, match.group(1).strip())
                assert os.path.exists(file_to_include), 'the path [%s] does not exist' % file_to_include
                contents = open(file_to_include).read()
                return wrap_included_contents(file_to_include, contents)

            return nl

        md = [handle_includes(l) for l in open(markdown_file).readlines()]
        md = [l.strip() for l in md]
        new_md = os.linesep.join(md)

        with open(output_markdown_file) as fp:
            fp.write(new_md)

        print('Finished.')



    process('../README.md', '../README-processed.md')
