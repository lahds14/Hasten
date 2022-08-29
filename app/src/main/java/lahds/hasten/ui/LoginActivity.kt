package lahds.hasten.ui

import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import lahds.hasten.R
import lahds.hasten.app.utils.Utilities
import lahds.hasten.databinding.ActivityLoginBinding
import lahds.hasten.ui.components.BaseFragment
import java.util.concurrent.TimeUnit

class LoginActivity : BaseFragment() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var authenticationId: String

    override fun createView(): View {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initialize() {
        initializeView()
        binding.buttonContinue.setOnClickListener {
            it.isEnabled = false
            val phoneNo = binding.textCountry.text.toString() + binding.textPhone.text.toString()
            binding.infoCode.setText(resources.getString(R.string.info_code) + phoneNo)
            if (phoneNo != "") {
                binding.infoCode.text = resources.getText(R.string.info_code, phoneNo)
                sendVerificationCode(phoneNo)
            }
        }

        binding.buttonVerify.setOnClickListener {
            binding.buttonVerify.isEnabled = false
            val code = "${binding.code1.text} + ${binding.code2.text} + ${binding.code3.text} + ${binding.code4.text} + ${binding.code5.text} + ${binding.code6.text}"
            if (code.isNotEmpty()) {
                verifyCode(code)
            }
        }
        binding.buttonCancel.setOnClickListener {
            binding.buttonContinue.isEnabled = true
            binding.buttonVerify.isEnabled = true
            Utilities.transition(binding.root)
            binding.layoutCode.visibility = View.GONE
            binding.layoutPhone.visibility = View.VISIBLE
        }
    }

    private fun sendVerificationCode(phoneNo: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNo)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    val code = credential.smsCode
                    if (code != null) {
                        binding.code1.setText("${code[0]}")
                        binding.code2.setText("${code[1]}")
                        binding.code3.setText("${code[2]}")
                        binding.code4.setText("${code[3]}")
                        binding.code5.setText("${code[4]}")
                        binding.code6.setText("${code[5]}")
                        binding.buttonVerify.isEnabled = false
                        verifyCode(code)
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Snackbar.make(binding.root, e.message.toString(), Snackbar.LENGTH_LONG).show()
                }

                override fun onCodeSent(verificationId: String, token: ForceResendingToken) {
                    super.onCodeSent(verificationId, token)
                    authenticationId = verificationId
                    Utilities.transition(binding.root)
                    binding.layoutPhone.visibility = View.GONE
                    binding.layoutCode.visibility = View.VISIBLE
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(authenticationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()
            ) { task ->
                if (task.isSuccessful) {
                    if (auth.currentUser != null) {
                        presentFragment(EditProfileActivity(), false)
                    }
                } else {
                    Snackbar.make(binding.root, "An error occurred.", Snackbar.LENGTH_LONG).show()
                }
            }
    }

    private fun initializeView() {
        Utilities.animateClick(binding.buttonContinue)
        Utilities.animateClick(binding.buttonCancel)
        Utilities.animateClick(binding.buttonVerify)
    }
}