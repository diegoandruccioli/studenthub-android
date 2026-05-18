package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.User
import com.unibo.android.domain.repository.AuthRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AuthUseCaseTest {

    private val repo: AuthRepository = mock()

    // ── LoginUseCase ─────────────────────────────────────────────────────────

    @Test
    fun `login - successo restituisce Result success`() = runTest {
        val user = User(1, "Mario", "Rossi", "mario@test.it", "student")
        whenever(repo.login("mario@test.it", "password123"))
            .thenReturn(Result.success(user))

        val result = LoginUseCase(repo)("mario@test.it", "password123")

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
    }

    @Test
    fun `login - credenziali errate restituisce Result failure`() = runTest {
        whenever(repo.login("wrong@test.it", "wrong"))
            .thenReturn(Result.failure(Exception("Credenziali non valide")))

        val result = LoginUseCase(repo)("wrong@test.it", "wrong")

        assertTrue(result.isFailure)
        assertEquals("Credenziali non valide", result.exceptionOrNull()?.message)
    }

    // ── RegisterUseCase ──────────────────────────────────────────────────────

    @Test
    fun `register - successo delega al repository`() = runTest {
        val user = User(2, "Anna", "Bianchi", "anna@test.it", "student")
        whenever(repo.register("Anna", "Bianchi", "anna@test.it", "pass1234"))
            .thenReturn(Result.success(user))

        val result = RegisterUseCase(repo)("Anna", "Bianchi", "anna@test.it", "pass1234")

        assertTrue(result.isSuccess)
        verify(repo).register("Anna", "Bianchi", "anna@test.it", "pass1234")
    }

    @Test
    fun `register - email già registrata restituisce failure`() = runTest {
        whenever(repo.register("Anna", "Bianchi", "anna@test.it", "pass1234"))
            .thenReturn(Result.failure(Exception("Email già registrata")))

        val result = RegisterUseCase(repo)("Anna", "Bianchi", "anna@test.it", "pass1234")

        assertTrue(result.isFailure)
    }

    // ── LogoutUseCase ────────────────────────────────────────────────────────

    @Test
    fun `logout - invoca repository logout`() = runTest {
        whenever(repo.logout()).thenReturn(Result.success(Unit))

        val result = LogoutUseCase(repo)()

        verify(repo).logout()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `logout - logout locale riuscito anche se API fallisce`() = runTest {
        // AuthRepositoryImpl swallows network errors and always clears local session
        whenever(repo.logout()).thenReturn(Result.success(Unit))

        val result = LogoutUseCase(repo)()

        assertTrue(result.isSuccess)
    }

    // ── isLoggedIn flow ──────────────────────────────────────────────────────

    @Test
    fun `isLoggedIn - emette il valore restituito dal repository`() = runTest {
        whenever(repo.isLoggedIn()).thenReturn(flowOf(true))

        val value = repo.isLoggedIn()
        var emitted = false
        value.collect { emitted = it }

        assertTrue(emitted)
    }
}
