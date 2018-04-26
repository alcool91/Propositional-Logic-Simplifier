import cloudconvert.api

api = cloudconvert.Api('nS9gRTrGq0r2ozqaG7jpkJmtdmcx944OW5bburss-VaTlbgwJ6huAdVlx76Cd9TrkhiZTyUqfn0lXntKoHrHjQ')

process = api.convert({
     "inputformat": "tex",
      "outputformat": "pdf",
      "input": "upload",
      "file": open('tt.tex', 'rb')
     })
process.wait()
process.download()
