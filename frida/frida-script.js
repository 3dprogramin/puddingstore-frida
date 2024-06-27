Java.perform(() => {
    const LoginActivity = Java.use('ro.steinbach.pudding.LoginActivity')
    LoginActivity.sendLoginRequest.implementation = function (apiUrl, email, password) {

        const response = this.sendLoginRequest(apiUrl, email, password)

//      console.log(response)

        if (response) {
            console.log(email, password)
        }

        return response
    }
})
