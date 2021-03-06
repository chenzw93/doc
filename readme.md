### create a new repository on the command line



```
echo "# doc" >> README.md
git init
git add README.md
git commit -m "first commit"
git branch -M main
git remote add origin https://github.com/chenzw93/doc.git
git push -u origin main
```

### push an existing repository from the command line



```
git remote add origin https://github.com/chenzw93/doc.git
git branch -M main
git push -u origin main
```

### import code from another repository

You can initialize this repository with code from a Subversion, Mercurial, or TFS project.

[Import code](https://github.com/chenzw93/doc/import)

```shell
git config --global --unset http.proxy
git config --global --unset https.proxy
git config --global http.sslVerify "false"
```

